# 1. 사용자 트래픽 (app-blue의 8080 포트)
set $service_url http://app-blue:8080;

# 2. 프로메테우스 모니터링 트래픽 (app-blue의 9292 포트)
set $management_url http://app-blue:9292;