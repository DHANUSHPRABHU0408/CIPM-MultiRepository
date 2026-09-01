package cipm.consistency.cpr.javapcm;

import java.util.ArrayList;
import java.util.List;
import cipm.consistency.commitintegration.PropagationTimingProvider;
import cipm.consistency.commitintegration.settings.CommitIntegrationSettingsContainer;
import cipm.consistency.commitintegration.settings.SettingKeys;
import mir.reactions.all.AllChangePropagationSpecification;
import tools.cipm.seff.Java2PcmMethodBodyChangePreprocessor;
import tools.cipm.seff.extended.ExtendedJava2PcmMethodBodyChangePreprocessor;
import tools.cipm.seff.finegrained.FineGrainedJava2PcmMethodBodyChangePreprocessor;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.change.propagation.ResourceAccess;

public class CommitIntegrationJavaPCMChangePropagationSpecification
extends AllChangePropagationSpecification
implements PropagationTimingProvider {

    private Java2PcmMethodBodyChangePreprocessor bodyTransformation;

    private final List<PropagationTimingProvider.PropagationTiming>
    propagationTimings = new ArrayList<>();

    

    @Override
    protected void setup() {
        super.setup();

        if (CommitIntegrationSettingsContainer.getSettingsContainer()
                .getPropertyAsBoolean(
                        SettingKeys.PERFORM_FINE_GRAINED_SEFF_RECONSTRUCTION)) {

            this.bodyTransformation =
                    new FineGrainedJava2PcmMethodBodyChangePreprocessor();

        } else {

            this.bodyTransformation =
                    new ExtendedJava2PcmMethodBodyChangePreprocessor();
        }
    }

    @Override
    public boolean doesHandleChange(
            EChange change,
            EditableCorrespondenceModelView<Correspondence> correspondenceModel) {

        return super.doesHandleChange(change, correspondenceModel)
                || this.bodyTransformation.doesHandleChange(
                        change,
                        correspondenceModel);
    }

    @Override
    public void propagateChange(
            EChange change,
            EditableCorrespondenceModelView<Correspondence> correspondenceModel,
            ResourceAccess resourceAccess) {

        String changeType =
                change.getClass().getSimpleName();

        long start = System.nanoTime();

        super.propagateChange(
                change,
                correspondenceModel,
                resourceAccess);

        long end = System.nanoTime();

        propagationTimings.add(
                new PropagationTimingProvider.PropagationTiming(
                        "super.propagateChange",
                        changeType,
                        (end - start) / 1_000_000.0));

        if (this.bodyTransformation.doesHandleChange(
                change,
                correspondenceModel)) {

            start = System.nanoTime();

            this.bodyTransformation.propagateChange(
                    change,
                    correspondenceModel,
                    resourceAccess);

            end = System.nanoTime();

            propagationTimings.add(
                    new PropagationTimingProvider.PropagationTiming(
                            "bodyTransformation.propagateChange",
                            changeType,
                            (end - start) / 1_000_000.0));
        }
    }

    @Override
    public List<PropagationTimingProvider.PropagationTiming>
            getPropagationTimings() {

        return new ArrayList<>(propagationTimings);
    }

    @Override
    public void clearPropagationTimings() {
        propagationTimings.clear();
    }
}