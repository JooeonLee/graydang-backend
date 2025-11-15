# 1. 사용자 트래픽 (app-green의 8080 포트)
set $service_url http://app-green:8080;

# 2. 프로메테우스 모니터링 트래픽 (app-green의 9293 포트)
set $management_url http://app-green:9293;