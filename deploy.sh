#!/bin/bash

# 실행되는 모든 명령어를 로그에 출력합니다.
set -x

# 스크립트 실행 중 오류가 발생하면 즉시 중단
set -e

# 1. 현재 실행중인 컨테이너 확인 (Blue or Green)
# docker ps 명령어와 grep을 이용해 'blue' 문자열이 포함된 컨테이너가 실행 중인지 확인
EXISTING_BLUE=$(docker ps | grep blue || true)

# 기본값을 Green으로 설정
IDLE_PROFILE="green"
# 관리 포트 변수 추가
IDLE_MANAGEMENT_PORT=9293
CURRENT_PROFILE="blue"

# 만약 실행중인 blue 컨테이너가 없다면? (첫 배포 또는 green이 실행중인 경우)
if [ -z "$EXISTING_BLUE" ]; then
    IDLE_PROFILE="blue"
    IDLE_MANAGEMENT_PORT=9292
    CURRENT_PROFILE="green"
fi

echo ">>> 현재 실행중인 서버: ${CURRENT_PROFILE}"
echo ">>> 새로 배포할 서버: ${IDLE_PROFILE} (관리 포트: ${IDLE_MANAGEMENT_PORT})"

# 2. 새로 배포할 Docker 이미지 태그를 환경변수로 받음 (GitHub Actions에서 전달)
# 예: ./deploy.sh 1.0.0
export DOCKER_IMAGE_TAG=$1

if [ -z "$DOCKER_IMAGE_TAG" ]; then
  echo ">>> ERROR: 배포할 Docker 이미지 태그를 입력해주세요."
  exit 1
fi

echo ">>> Docker 이미지 태그: ${DOCKER_IMAGE_TAG}"

# 3. docker-compose.yml을 이용해 새로운 버전의 컨테이너 실행
# --profile 옵션으로 blue 또는 green 서비스만 선택적으로 실행
echo ">>> ${IDLE_PROFILE} 서버(컨테이너)를 실행합니다. Management Port: ${IDLE_MANAGEMENT_PORT}"
docker compose --profile ${IDLE_PROFILE} up -d --build

# 4. 새로운 컨테이너가 정상적으로 실행되었는지 헬스 체크
echo ">>> ${IDLE_PROFILE} 서버 헬스 체크..."
echo ">>> 최대 60초 동안 5초 간격으로 헬스 체크를 시도합니다."

for i in {1..12}; do
    # curl로 헬스 체크 시도
    STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:${IDLE_MANAGEMENT_PORT}/actuator/health)

    if [ ${STATUS_CODE} -eq 200 ]; then
        echo ">>> 헬스 체크 성공! (상태 코드: ${STATUS_CODE})"
        break # 성공 시 루프 탈출
    else
        echo ">>> 아직 준비되지 않았습니다... (${i}/12)"
        # 마지막 시도(12번째)에도 실패하면 배포 실패 처리
        if [ ${i} -eq 12 ]; then
            echo ">>> 헬스 체크에 최종 실패했습니다. 배포를 중단합니다."
            echo ">>> ${IDLE_PROFILE} 컨테이너의 최근 로그 50줄:"
            # 실패 원인 파악을 위해 컨테이너 로그 출력
            docker logs --tail 50 graydang-app-${IDLE_PROFILE}
            # 실패한 컨테이너 종료
            docker compose --profile ${IDLE_PROFILE} down
            exit 1
        fi
        sleep 5 # 5초 대기 후 재시도
    fi
done

# 5. 헬스 체크 성공 시, Nginx 설정 변경하여 트래픽 전환
echo ">>> Nginx 리버스 프록시 설정을 ${IDLE_PROFILE} 서버로 변경합니다."
# service-blue.inc 또는 service-green.inc 파일을 service.inc 로 덮어쓰기
cp ./nginx/conf.d/service-${IDLE_PROFILE}.inc ./nginx/conf.d/service.inc

# 6. Nginx 컨테이너에 reload 시그널 전송 (설정 다시 읽기)
# docker exec <컨테이너 이름> <명령어>
echo ">>> Nginx 설정을 리로드합니다."
docker exec nginx nginx -s reload

# 7. 기존에 실행중이던 구 버전 컨테이너 종료
echo ">>> 기존 ${CURRENT_PROFILE} 서버(컨테이너)를 종료합니다."
EXISTING_CONTAINER=$(docker compose ps -q --profile ${CURRENT_PROFILE})
if [ -n "$EXISTING_CONTAINER" ]; then
  docker compose --profile ${CURRENT_PROFILE} down
else
    echo ">>> 기존 ${CURRENT_PROFILE} 서버가 없어 종료를 건너뜁니다."
fi

echo ">>> ✅ 배포가 성공적으로 완료되었습니다."

# 명령어 출력을 중단합니다.
set +x
