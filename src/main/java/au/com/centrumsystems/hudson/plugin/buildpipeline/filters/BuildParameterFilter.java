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

/**
 * Filters builds by build params that are specified as the request params
 * @author mrozar
 */
public class BuildParameterFilter implements Predicate<AbstractBuild<?, ?>> {

    @Override
    public boolean apply(AbstractBuild<?, ?> build) {
        final StaplerRequest req = Stapler.getCurrentRequest();
        final Map reqParams = req.getParameterMap();
        if (reqParams != null && !reqParams.isEmpty()) {
            final List<ParametersAction> parametersActions = build.getActions(ParametersAction.class);
            if (parametersActions != null && !parametersActions.isEmpty()) {
                final List<ParameterValue> parameters = parametersActions.get(0).getParameters();
                for (ParameterValue pValue: parameters) {
                    if (req.getParameter(pValue.getName()) != null
                            && StringParameterValue.class.isAssignableFrom(pValue.getClass())) {
                        final String paramValue = ((StringParameterValue) pValue).value;
                        final String reqParamValue = req.getParameter(pValue.getName());
                        return isWildcardFilter(reqParamValue)
                                ? isMatchingWildcardFilter(paramValue, reqParamValue) : paramValue.equals(reqParamValue);
                    }
                }
            }
        }

        return true;
    }

    /**
     * Does param value contains wildcard expression?
     * @param reqParamValue the param value
     * @return true if it does boolean
     */
    private boolean isWildcardFilter(String reqParamValue) {
        return reqParamValue != null && reqParamValue.contains("*");
    }

    /**
     * Is build param value matching the requested value
     *
     * @param buildParamValue the build parameter value
     * @param reqParamValue the expected value from the request
     * @return true if build is matching
     */
    private boolean isMatchingWildcardFilter(String buildParamValue, String reqParamValue) {
        final String[] items = reqParamValue.replace("*", "").split(",");
        for (String item: items) {
            if (!buildParamValue.contains(item)) {
                return false;
            }
        }

        return true;
    }
}
