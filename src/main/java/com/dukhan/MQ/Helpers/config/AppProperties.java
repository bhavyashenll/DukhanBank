package com.dukhan.MQ.Helpers.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Mock mock = new Mock();
    private Rate rate = new Rate();

    public Mock getMock() {
        return mock;
    }

    public void setMock(Mock mock) {
        this.mock = mock;
    }

    public Rate getRate() {
        return rate;
    }

    public void setRate(Rate rate) {
        this.rate = rate;
    }

    public static class Mock {
        private boolean isMockResponse = false;

        public boolean isMockResponse() {
            return isMockResponse;
        }

        public void setIsMockResponse(boolean isMockResponse) {
            this.isMockResponse = isMockResponse;
        }
    }

    public static class Rate {
        private int digits = 6;

        public int getDigits() {
            return digits;
        }

        public void setDigits(int digits) {
            this.digits = digits;
        }
    }
}


