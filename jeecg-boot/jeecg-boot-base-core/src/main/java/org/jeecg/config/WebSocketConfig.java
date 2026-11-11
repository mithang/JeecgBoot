package org.jeecg.config;

import org.jeecg.config.filter.WebsocketFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @Description: WebSocketConfig
 * @author: jeecg-boot
 */
@Configuration
public class WebSocketConfig {
    /**
     * 	Inject ServerEndpointExporter,
     * 	Bean này sẽ tự động đăng ký các endpoint đã sử dụng@ServerEndpoint được khai báo bằng annotation Websocket endpoint
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    public WebsocketFilter websocketFilter(){
        return new WebsocketFilter();
    }

    @Bean
    public FilterRegistrationBean getFilterRegistrationBean(){
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(websocketFilter());
        //TODO Tạm thời comment lại, kiểm tra vấn đề socket trên môi trường production hay bị ngắt
        bean.addUrlPatterns("/taskCountSocket/*", "/websocket/*","/eoaSocket/*","/eoaNewChatSocket/*", "/newsWebsocket/*", "/dragChannelSocket/*", "/vxeSocket/*");
        return bean;
    }

}
