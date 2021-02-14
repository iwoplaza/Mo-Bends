package goblinbob.mobends.core.kumo.keyframe.node;

import goblinbob.mobends.core.animation.keyframe.Bone;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.data.PropertyStorage;
import goblinbob.mobends.core.kumo.*;
import goblinbob.mobends.forge.BasePropertyKeys;

import java.util.ArrayList;
import java.util.List;

public class MovementKeyframeNode<D extends IEntityData> implements INodeState<D>
{
    public final KeyframeAnimation animation;
    private int animationDuration;
    private final int startFrame;
    private final float playbackSpeed;
    private List<ConnectionState<D>> connections = new ArrayList<>();

    /**
     * Progress counted in keyframes.
     */
    private float progress;

    public MovementKeyframeNode(IKumoInstancingContext<D> context, MovementKeyframeNodeTemplate template)
    {
        this(template.animationKey != null ? context.getAnimation(template.animationKey) : null,
                template.startFrame,
                template.playbackSpeed);
    }

    public MovementKeyframeNode(KeyframeAnimation animation, int startFrame, float playbackSpeed)
    {
        this.animation = animation;
        this.startFrame = startFrame;
        this.playbackSpeed = playbackSpeed;

        if (animation != null)
        {
            // Evaluating the animation duration.
            this.animationDuration = 0;
            for (Bone bone : animation.bones.values())
            {
                if (bone.getKeyframes().length > this.animationDuration)
                    this.animationDuration = bone.getKeyframes().length;
            }
        }

        this.progress = this.startFrame;
    }

    @Override
    public Iterable<ConnectionState<D>> getConnections()
    {
        return connections;
    }

    @Override
    public KeyframeAnimation getAnimation()
    {
        return animation;
    }

    @Override
    public float getProgress(IKumoReadContext<D> context)
    {
        return progress;
    }

    @Override
    public boolean isAnimationFinished(IKumoReadContext<D> context)
    {
        return false;
    }

    @Override
    public void parseConnections(List<INodeState<D>> nodeStates, KeyframeNodeTemplate template, IKumoInstancingContext<D> context)
    {
        if (template.connections != null)
        {
            for (ConnectionTemplate connectionTemplate : template.connections)
            {
                this.connections.add(connectionTemplate.instantiate(nodeStates, context));
            }
        }
    }

    @Override
    public void start(IKumoContext<D> context)
    {
        this.progress = this.startFrame;
        for (ConnectionState<D> connection : connections)
        {
            connection.triggerCondition.onNodeStarted(context);
        }
    }

    @Override
    public void update(IKumoContext<D> context)
    {
        D data = context.getEntityData();
        PropertyStorage storage = data.getPropertyStorage();

        if (animation != null)
        {
            float limbSwing = storage.getFloatProperty(BasePropertyKeys.LIMB_SWING) * 0.6662F;

            this.progress = this.playbackSpeed * limbSwing;
            this.progress %= this.animationDuration - 1;
        }
    }
}