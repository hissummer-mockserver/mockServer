package com.hissummer.mockserver.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

public class mockRuleCacheService {

    public static Cache<String, String > ruleCache = CacheBuilder.newBuilder()
            .expireAfterAccess(60, TimeUnit.MINUTES).build();
    public static Cache<String, String > conditionRuleCache = CacheBuilder.newBuilder()
            .expireAfterAccess(60, TimeUnit.MINUTES).build();

}
