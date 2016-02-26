package au.com.centrumsystems.hudson.plugin.buildpipeline.filters;

import com.google.common.base.Predicate;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.User;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Filters builds by the user who triggered them. The desired user is passed in via 'user' request param
 * @author mrozar
 */
public class UserFilter implements Predicate<AbstractBuild<?, ?>> {
    @Override
    public boolean apply(AbstractBuild<?, ?> build) {
        final StaplerRequest req = Stapler.getCurrentRequest();
        String user = req.getParameter("user");

        if ("me".equals(user) && User.current() != null) {
            user = User.current().getId();
        }

        final Cause.UserCause userCause = build.getCause(Cause.UserCause.class);
        final Cause.UserIdCause userIdCause = build.getCause(Cause.UserIdCause.class);
        if (StringUtils.isNotBlank(user) && userCause != null) {
            return user.equals(userCause.getUserName());
        }
        if (StringUtils.isNotBlank(user) && userIdCause != null) {
            return user.equals(userIdCause.getUserId());
        }

        return true;
    }
}
