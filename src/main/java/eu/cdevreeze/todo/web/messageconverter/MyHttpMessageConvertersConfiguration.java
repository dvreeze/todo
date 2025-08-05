/*
 * Copyright 2025-2025 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.todo.web.messageconverter;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Configuration class containing a {@link HttpMessageConverters} bean.
 *
 * @author Chris de Vreeze
 */
@Configuration(proxyBeanMethods = false)
public class MyHttpMessageConvertersConfiguration {

    // See https://github.com/FasterXML/jackson-datatypes-collections

    @Bean
    public HttpMessageConverters customConverters() {
        HttpMessageConverter<?> converter = new MappingJackson2HttpMessageConverter(
                JsonMapper.builder()
                        .addModule(new GuavaModule())
                        .addModule(new Jdk8Module())
                        .addModule(new JavaTimeModule())
                        .build()
        );
        return new HttpMessageConverters(converter);
    }
}
