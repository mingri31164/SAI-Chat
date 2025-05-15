package com.mingri.web.hook.filter;

import com.mingri.web.handler.AuthenticationEntryPointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig  {

	@Autowired
	private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
	@Autowired
	private AuthenticationEntryPointImpl authenticationEntryPoint;
	@Autowired
	private AccessDeniedHandlerImpl accessDeniedHandler;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}



	//我们在 SysUserServiceImpl 中 注入了 AuthenticationManager。
	//而 AuthenticationManager 是我们 SecurityConfig 里用 @Bean 方法定义的，并且它 依赖 HttpSecurity。
	//Spring 在创建 HttpSecurity 的时候，会 反过来触发我们的 SysUserServiceImpl，于是形成了循环依赖。
//	@Bean（考虑删除：不需要自己显式地声明@BeanAuthenticationManager，
//	在我们的SysUserServiceImpl中，直接使用构造器注入AuthenticationManager即可）
//	public AuthenticationManager authManager(HttpSecurity http) throws Exception {
//		AuthenticationManagerBuilder authenticationManagerBuilder =
//				http.getSharedObject(AuthenticationManagerBuilder.class);
//		return authenticationManagerBuilder.build();
//	}



	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}





// SpingBoot3x 默认使用 WebFlux 模式，而不是使用 SpringMVC 模式。此处只适用于SpringBoot2x
//	@Bean
//	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//		http
//				.csrf().disable() // 关闭 CSRF
//				.sessionManagement().
//				sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 使用无状态会话
//				.and()
//				.authorizeRequests()
//				.antMatchers("/model/v1/user/helper","/model/v1/user/register","/model/v1/file",
//						"/model/v1/file","/model/v1/model/**",
//						"/ws/**",
//						"/v2/model-docs",
//						"/swagger-resources/configuration/ui",
//						"/swagger-resources",
//						"/swagger-resources/configuration/security",
//						"/doc.html",
//						"/swagger-ui.html",
//						"/webjars/**").anonymous()// 接口允许匿名访问（已登录不可访问，未登录可以）
////				.antMatchers("/sys-user").hasAuthority("system:dept:list") //配置指定路径接口需要权限访问
//				.anyRequest().authenticated(); // 其他请求需要认证
//
//		//把token校验过滤器添加到过滤器链中
//		http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
//
//		//配置认证失败处理器
//		http.exceptionHandling()
//				.authenticationEntryPoint(authenticationEntryPoint)
//				.accessDeniedHandler(accessDeniedHandler);
//
//		//配置跨域
//		http.cors().configurationSource(corsConfigurationSource());
//
//		return http.build();
//
//	}



	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable()) // 关闭 CSRF
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 使用无状态会话
				)
				.authorizeHttpRequests(auth -> auth
//								.anyRequest().permitAll() // 放行所有（测试）
						.requestMatchers(
								"/model/v1/user/login",
								"/model/v1/user/register",
								"/model/v1/file",
								"/model/v1/common/**",
								"/ws/**",
								//swagger3相关接口
								"/v3/model-docs/**",
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/doc.html",
								"/webjars/**"
						).permitAll() // 允许匿名访问
						// .requestMatchers("/sys-user").hasAuthority("system:dept:list") // 需要特定权限
						.anyRequest().authenticated() // 其他请求需要认证
				)
				.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler)
				)
				.cors(cors -> cors.configurationSource(corsConfigurationSource()));

		return http.build();
	}



	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowCredentials(false);
		configuration.addAllowedOrigin("*");
		configuration.addAllowedHeader("*");
		configuration.addAllowedMethod("*");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
