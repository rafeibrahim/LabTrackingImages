package fi.metropolia.labtrackingimages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainActivity : AppCompatActivity() {
    lateinit var fragment: ArFragment
    lateinit var fitToScanImageView: ImageView
    lateinit var testRenderable: ViewRenderable
    lateinit var testRenderable2: ViewRenderable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragment = supportFragmentManager.findFragmentById(R.id.arimage_fragment) as ArFragment
        fitToScanImageView = findViewById<ImageView>(R.id.fit_to_scan_img)

        val renderableFuture = ViewRenderable.builder()
            .setView(this, R.layout.rendtext)
            .build()
        renderableFuture.thenAccept {
            testRenderable = it
        }

        val renderableFuture2 = ViewRenderable.builder()
            .setView(this, R.layout.rendtext2)
            .build()
        renderableFuture2.thenAccept {it -> testRenderable2 = it}

        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            frameUpdate()
        }
    }

    private fun frameUpdate() {
        val arFrame = fragment.arSceneView.arFrame

        if (arFrame == null || arFrame.camera.trackingState != TrackingState.TRACKING){
            return
        }

        val updatedAugmentedImages = arFrame.getUpdatedTrackables(AugmentedImage::class.java)
        updatedAugmentedImages.forEach{
            when(it.trackingState) {
                TrackingState.PAUSED -> {
                    val text = "Detected Image:" + it.name + " -need more info"
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }

                TrackingState.STOPPED -> {
                    val text = "Tracking stopped: " + it.name
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }

                TrackingState.TRACKING -> {
                    val anchors = it.anchors
                    if (anchors.isEmpty()) {
                        fitToScanImageView.visibility = View.GONE
                        val pose = it.centerPose
                        val anchor = it.createAnchor(pose)
                        val anchorNode = AnchorNode(anchor)
                        //anchorNode.setParent(anchorNode)
                        anchorNode.setParent(fragment.arSceneView.scene)
                        val imgNode= TransformableNode(fragment.transformationSystem)
                        imgNode.setParent(anchorNode)
                        if (it.name == "sofa") {
                            imgNode.renderable = testRenderable
                        }
                        if (it.name == "earth") {
                            imgNode.renderable = testRenderable2
                        }
                    }
                }
            }
        }
    }
}