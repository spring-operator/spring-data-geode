/*
 * Copyright 2017-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.gemfire.config.annotation;

import java.util.Optional;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.gemfire.config.annotation.support.AutoConfiguredAuthenticationInitializer;
import org.springframework.util.StringUtils;

/**
 * The {@link AutoConfiguredAuthenticationConfiguration} class is a Spring {@link Configuration @Configuration} class
 * that auto-configures Pivotal GemFire / Apache Geode Authentication by providing a implementation
 * of the {@link org.apache.geode.security.AuthInitialize} interface along with setting the necessary GemFire / Geode
 * properties.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.geode.security.AuthInitialize
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Condition
 * @see org.springframework.context.annotation.Conditional
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.core.env.Environment
 * @see org.springframework.data.gemfire.config.annotation.EnableBeanFactoryLocator
 * @see org.springframework.data.gemfire.config.annotation.support.AutoConfiguredAuthenticationInitializer
 * @see org.springframework.data.gemfire.util.PropertiesBuilder
 * @since 2.0.0
 */
@Configuration
@Import(BeanFactoryLocatorConfiguration.class)
@Conditional(AutoConfiguredAuthenticationConfiguration.AutoConfiguredAuthenticationCondition.class)
@SuppressWarnings("unused")
public class AutoConfiguredAuthenticationConfiguration {

	protected static final String AUTO_CONFIGURED_AUTH_INIT_STATIC_FACTORY_METHOD =
		AutoConfiguredAuthenticationInitializer.class.getName().concat(".newAuthenticationInitializer");

	protected static final String SECURITY_CLIENT_AUTH_INIT = "security-client-auth-init";
	protected static final String SECURITY_PEER_AUTH_INIT = "security-peer-auth-init";

	@Bean
	public ClientCacheConfigurer authenticationCredentialsSettingClientCacheConfigurer(Environment environment) {
		return (beanName, beanFactory) -> setAuthenticationCredentials(beanFactory.getProperties(), environment);
	}

	@Bean
	public PeerCacheConfigurer authenticationCredentialsSettingPeerCacheConfigurer(Environment environment) {
		return (beanName, beanFactory) -> setAuthenticationCredentials(beanFactory.getProperties(), environment);
	}

	private void setAuthenticationCredentials(Properties gemfireProperties, Environment environment) {

		Optional.ofNullable(gemfireProperties)
			.filter(properties -> isMatch(environment))
			.ifPresent(properties -> {
				properties.setProperty(SECURITY_CLIENT_AUTH_INIT, AUTO_CONFIGURED_AUTH_INIT_STATIC_FACTORY_METHOD);
				properties.setProperty(SECURITY_PEER_AUTH_INIT, AUTO_CONFIGURED_AUTH_INIT_STATIC_FACTORY_METHOD);
			});
	}

	private static boolean isMatch(Environment environment) {

		return Optional.ofNullable(environment)
			.map(env -> env.getProperty(AutoConfiguredAuthenticationInitializer.SDG_SECURITY_USERNAME_PROPERTY))
			.map(StringUtils::hasText)
			.isPresent();
	}

	public static class AutoConfiguredAuthenticationCondition implements Condition {

		@Override
		public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
			return isMatch(conditionContext.getEnvironment());
		}
	}
}
