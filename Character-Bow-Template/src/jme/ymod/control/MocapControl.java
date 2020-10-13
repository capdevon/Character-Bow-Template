/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme.ymod.control;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.Bone;
import com.jme3.animation.LoopMode;
import com.jme3.scene.Spatial;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme.ymod.engine.Animation3;


/**
 *
 */
public class MocapControl extends AdapterControl {
    
    private static final Logger LOGGER = Logger.getLogger(MocapControl.class.getName());
    
    private AnimControl animControl;
    private AnimChannel animChannel;
    
    @Override
    public void setSpatial(Spatial sp) {
        super.setSpatial(sp);
        if (spatial != null) {
            this.animControl = getComponentInChild(AnimControl.class);
            this.animChannel = animControl.createChannel();
            System.out.println(spatial.getName() + " --Animations: " + animControl.getAnimationNames());
        }
    }
    
    public void setAnimation(String animName, LoopMode loopMode) {
        if (hasAnimation(animName)) {
            if (!animName.equals(animChannel.getAnimationName())) {
                animChannel.setAnim(animName, .15f);
                animChannel.setSpeed(1);
                animChannel.setLoopMode(loopMode);
            }
        }
    }

    public void setAnimation(Animation3 animation) {
        if (hasAnimation(animation.name)) {
            if (!animation.name.equals(animChannel.getAnimationName())) {
                animChannel.setAnim(animation.name, animation.blendTime);
                animChannel.setSpeed(animation.speed);
                animChannel.setLoopMode(animation.loopMode);
            }
        }
    }

    public void crossFade(Animation3 newAnim) {
        float dt = animChannel.getTime();
        animChannel.setAnim(newAnim.name, newAnim.blendTime);
        animChannel.setSpeed(newAnim.speed);
        animChannel.setLoopMode(newAnim.loopMode);
        animChannel.setTime(dt);
    }

    private boolean hasAnimation(String animName) {
        boolean result = animControl.getAnimationNames().contains(animName);
        if (!result) {
            LOGGER.log(Level.WARNING, "Cannot find animation named: {0}", animName);
        }
        return result;
    }

    public Bone getBone(String boneName) {
        return animControl.getSkeleton().getBone(boneName);
    }

    public Spatial getRootMotion() {
        return animControl.getSpatial();
    }

    public String getAnimationName() {
        return animChannel.getAnimationName();
    }

    public float getDeltaTime() {
        return animChannel.getTime() / animChannel.getAnimMaxTime();
    }

    public void addListener(AnimEventListener listener) {
        animControl.addListener(listener);
    }

    public void removeListener(AnimEventListener listener) {
        animControl.removeListener(listener);
    }
    
}