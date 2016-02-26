package au.com.centrumsystems.hudson.plugin.buildpipeline.filters;

import com.google.common.base.Predicate;
import hudson.model.AbstractBuild;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import java.util.List;
import java.util.Map;

public class BuildParameterFilter implements Predicate<AbstractBuild<?, ?>> {

    @Override
    public boolean apply(AbstractBuild<?, ?> build) {
        StaplerRequest req = Stapler.getCurrentRequest();
        Map reqParams = req.getParameterMap();
        if (reqParams != null && !reqParams.isEmpty()) {
            List<ParametersAction> parametersActions = build.getActions(ParametersAction.class);
            if (parametersActions != null && !parametersActions.isEmpty()) {
                List<ParameterValue> parameters = parametersActions.get(0).getParameters();
                for(ParameterValue pValue: parameters) {
                    if (req.getParameter(pValue.getName()) != null
                            && StringParameterValue.class.isAssignableFrom(pValue.getClass())) {
                        String paramValue = ((StringParameterValue) pValue).value;
                        String reqParamValue = req.getParameter(pValue.getName());
                        return isWildcardFilter(reqParamValue)
                                ? isMatchingWildcardFilter(paramValue, reqParamValue) : paramValue.equals(reqParamValue);
                    }
                }
            }
        }

        return true;
    }

    private boolean isWildcardFilter(String reqParamValue) {
        return reqParamValue != null && reqParamValue.contains("*");
    }

    private boolean isMatchingWildcardFilter(String buildParamValue, String reqParamValue) {
        String[] items = reqParamValue.replace("*", "").split(",");
        for (String item: items) {
            if (!buildParamValue.contains(item)) {
                return false;
            }
        }

        return true;
    }
}
