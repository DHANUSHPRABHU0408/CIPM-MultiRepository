package cipm.consistency.commitintegration;

import java.util.List;

public interface PropagationTimingProvider {

    List<PropagationTiming> getPropagationTimings();

    void clearPropagationTimings();

    class PropagationTiming {

        private final String methodName;
        private final String changeType;
        private final double executionTimeMs;

        public PropagationTiming(
                String methodName,
                String changeType,
                double executionTimeMs) {

            this.methodName = methodName;
            this.changeType = changeType;
            this.executionTimeMs = executionTimeMs;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getChangeType() {
            return changeType;
        }

        public double getExecutionTimeMs() {
            return executionTimeMs;
        }
    }
}