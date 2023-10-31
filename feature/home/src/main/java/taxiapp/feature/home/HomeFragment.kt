package taxiapp.feature.home

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import taxiapp.core.common.permissions.LocationTextProvider
import taxiapp.core.common.permissions.NotificationTextProvider
import taxiapp.core.common.permissions.PermissionManager
import taxiapp.core.model.Location
import taxiapp.feature.home.adapter.OnTariffClickedListener
import taxiapp.feature.home.adapter.TariffAdapter
import taxiapp.feature.home.databinding.FragmentHomeBinding
import taxiapp.feature.home.location.LocationService
import java.io.IOException
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding get() = requireNotNull(_binding)

    private var mapView: MapView? = null

    private val viewModel by viewModels<HomeViewModel>()

    @Inject
    lateinit var notificationTextProvider: NotificationTextProvider

    @Inject
    lateinit var locationTextProvider: LocationTextProvider

    private var circleAnnotations: List<CircleAnnotation>? = null
    private val circleAnnotationManager by lazy {
        mapView?.annotations?.createCircleAnnotationManager()
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var defaultZoomLevel = 14.0

    private var animator: ObjectAnimator? = null

    private val tariffAdapter by lazy(LazyThreadSafetyMode.NONE) {
        TariffAdapter(onClickListener)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            startAnimation()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {
            getAddress()
            stopAnimation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.contentLayout.updatePadding(
                left = 0,
                top = insets.top,
                right = 0,
                bottom = insets.bottom
            )

            WindowInsetsCompat.CONSUMED
        }
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.mapView) {
            logo.enabled = false
            compass.enabled = false
            attribution.enabled = false
            scalebar.enabled = false
            mapView = this
        }

        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            == Configuration.UI_MODE_NIGHT_YES
        ) {
            mapView?.getMapboxMap()?.loadStyleUri(
                Style.DARK
            )
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        setClickListeners()
        initTariffRv()
        setupGesturesListener()
        subscribeToFlow()
    }

    override fun onStart() {
        super.onStart()
        val permissionManager = PermissionManager(
            requireActivity(),
            requireContext(),
            requireActivity().activityResultRegistry,
            this,
            notificationTextProvider,
            locationTextProvider,
            ::createLocationRequest
        )
        permissionManager.requestPermissions()
    }

    override fun onDestroyView() {
        mapView?.gestures?.removeOnMoveListener(onMoveListener)
        mapView = null
        _binding = null
        super.onDestroyView()
    }

    private fun setClickListeners() {
        with(binding) {
            zoomInIv.setOnClickListener {
                defaultZoomLevel += 2
                setCameraPosition()
            }

            zoomOutIv.setOnClickListener {
                defaultZoomLevel -= 2
                setCameraPosition()
            }

            currentLocationIv.setOnClickListener {
                getCurrentLocation()
            }
        }
    }

    private fun subscribeToFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.locations.collect { location: Location ->
                    changeUserLocation(location)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.networkStatus.collect { isAvailable: Boolean ->
                    binding.noInternetView.isVisible = !isAvailable
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tempTariffs.collect { list ->
                    tariffAdapter.submitList(list)
                }
            }
        }
    }

    private fun changeUserLocation(location: Location) {
        val point = Point.fromLngLat(location.longitude, location.latitude)

        val circleAnnotationOptions = CircleAnnotationOptions()
            .withPoint(point)
            .withCircleRadius(8.0)
            .withCircleColor("#2196F3")
            .withCircleStrokeWidth(2.0)
            .withCircleStrokeColor("#ffffff")

        val circleAnnotationOptionsOpacity = CircleAnnotationOptions()
            .withPoint(point)
            .withCircleRadius(30.0)
            .withCircleColor("#2196F3")
            .withCircleOpacity(0.3)

        val cameraPosition = CameraOptions.Builder()
            .zoom(defaultZoomLevel)
            .center(circleAnnotationOptions.getPoint())
            .build()

        mapView?.getMapboxMap()?.flyTo(cameraPosition)

        if (circleAnnotations == null) {
            circleAnnotations = circleAnnotationManager?.create(
                options = listOf(
                    circleAnnotationOptionsOpacity,
                    circleAnnotationOptions
                )
            )
        } else {
            circleAnnotations?.forEach {
                it.point = point
            }
            circleAnnotations?.let { circleAnnotationManager?.update(it) }
        }
    }

    private fun createLocationRequest() {
        val locationRequest = LocationRequest.Builder(5000).build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        builder.setAlwaysShow(true)

        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            onLocationEnabled()
        }

        task.addOnFailureListener { exception: Exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest
                        .Builder(exception.resolution).build()
                    resolutionForResult.launch(intentSenderRequest)
                } catch (throwable: Throwable) {
                    Log.e("HomeFragment", "createLocationRequest: ", throwable.cause)
                }
            }

        }
    }

    private fun onLocationEnabled() {
        val intent = Intent(requireContext(), LocationService::class.java)
        requireActivity().startService(intent)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: android.location.Location? ->
            if (location != null) {
                defaultZoomLevel = 14.0
                val cameraPosition = CameraOptions.Builder()
                    .zoom(defaultZoomLevel)
                    .center(Point.fromLngLat(location.longitude, location.latitude))
                    .build()

                mapView?.getMapboxMap()?.flyTo(cameraPosition)
            }
        }
    }

    private fun setCameraPosition() {
        if (defaultZoomLevel < MIN_ZOOM_LEVEL) {
            defaultZoomLevel = MIN_ZOOM_LEVEL
        } else if (defaultZoomLevel > MAX_ZOOM_LEVEL) {
            defaultZoomLevel = MAX_ZOOM_LEVEL
        }

        val cameraPosition = CameraOptions.Builder()
            .zoom(defaultZoomLevel)
            .build()

        mapView?.getMapboxMap()?.flyTo(cameraPosition)
    }

    private fun setupGesturesListener() {
        mapView?.gestures?.addOnMoveListener(onMoveListener)
    }

    private fun initTariffRv() {
        with(binding.tariffRv) {
            setHasFixedSize(true)
            adapter = tariffAdapter
        }
    }

    private fun startAnimation() {
        binding.startLocationTv.text = resources.getString(R.string.search)

        ObjectAnimator.ofFloat(binding.markerImage, "translationY", -20f).apply {
            duration = 500
            start()
        }

        ObjectAnimator.ofFloat(binding.markerShadow, "scaleY", 2F).apply {
            duration = 500
            start()
        }

        ObjectAnimator.ofFloat(binding.markerShadow, "scaleX", 2F).apply {
            duration = 500
            start()
        }

        animator = ObjectAnimator.ofInt(binding.markerImage, "colorFilter", Color.YELLOW)
            .apply {
                duration = 700
                setEvaluator(ArgbEvaluator())
                repeatCount = Animation.REVERSE
                repeatCount = Animation.INFINITE
                start()
            }
    }

    private fun stopAnimation() {
        animator?.cancel()
        binding.markerImage.clearColorFilter()

        ObjectAnimator.ofFloat(binding.markerImage, "translationY", 0f).apply {
            duration = 500
            start()
        }
        ObjectAnimator.ofFloat(binding.markerShadow, "scaleY", 1F).apply {
            duration = 500
            start()
        }
        ObjectAnimator.ofFloat(binding.markerShadow, "scaleX", 1F).apply {
            duration = 500
            start()
        }
    }

    private fun getAddress() {
        val center: Point = mapView?.getMapboxMap()?.cameraState?.center ?: return
        val latitude = center.latitude()
        val longitude = center.longitude()

        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        binding.startLocationTv.text = try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses!!.size > 0) {
                val address = addresses[0]
                val fullAddress = address.getAddressLine(0).split(",")
                "${fullAddress[0]},${fullAddress[1]}"
            } else {
                resources.getString(R.string.map_dot)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            resources.getString(R.string.map_dot)
        }
    }

    private val resolutionForResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult: ActivityResult ->
        if (activityResult.resultCode == AppCompatActivity.RESULT_OK) {
            onLocationEnabled()
        } else {
            createLocationRequest()
        }
    }

    private val onClickListener = object : OnTariffClickedListener {
        override fun onClick(id: Int) {
            viewModel.changeTempTariffs(id)
        }
    }

    companion object {
        const val MAX_ZOOM_LEVEL = 22.0
        const val MIN_ZOOM_LEVEL = 0.0
    }
}