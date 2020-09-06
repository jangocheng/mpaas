package ghost.framework.web.mvc.nginx.ui.plugin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Resource
	private AdminInterceptor adminInterceptor;
	@Resource
	private FrontInterceptor frontInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 自定义拦截器，添加拦截路径和排除拦截路径
		registry.addInterceptor(adminInterceptor)//
				.addPathPatterns("/adminPage/**") //
				.excludePathPatterns("/lib/**") //
				.excludePathPatterns("/js/**")//
				.excludePathPatterns("/img/**")//
				.excludePathPatterns("/css/**");

		registry.addInterceptor(frontInterceptor)//
				.addPathPatterns("/**")//
				.excludePathPatterns("/lib/**") //
				.excludePathPatterns("/js/**")//
				.excludePathPatterns("/img/**")//
				.excludePathPatterns("/css/**");

//		registry.addInterceptor(new LocaleInterceptor()).addPathPatterns("/**");
	}

	@Bean
	public LocaleResolver localeResolver(MyLocaleResolver myLocaleResolver) {
		return myLocaleResolver;
	}
}