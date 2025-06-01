package backend.academy.configuration;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GlobalConstants {
    /** Chat id in telegram. Used with {@link Long} format */
    public static final String TG_CHAT_ID = "Tg-Chat-Id";

    /** Github api version */
    public static final String GITHUB_API_VERSION = "X-GitHub-Api-Version";

    public static final String USER_IP_CONTEXT = "User-Ip";
}
