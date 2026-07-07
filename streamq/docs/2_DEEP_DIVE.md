# 3. OS 레벨 딥다이브: WebFlux의 진실

이 문서는 코드를 넘어, 애플리케이션 하단(OS/Kernel)에서 일어나는 현상과 Spring WebFlux의 내부 원리를 기록합니다. 심층 면접 대비용 아카이브입니다.

## 1. I/O 모델 (Blocking vs Non-Blocking)
(내용 작성 예정)
- Context Switching의 비용과 CPU 상태 변화 (Wait/Run)
- Linux `epoll`과 소켓 파일 디스크립터(File Descriptor) 모니터링 원리

## 2. Spring MVC vs Spring WebFlux
(내용 작성 예정)
- Tomcat(Thread-per-Request)의 한계와 OOM
- Netty Event Loop의 C10K 문제 해결 원리

## 3. 최악의 충돌: 비동기 세상에 동기 코드가 들어왔을 때
(내용 작성 예정)
- **실증 실험**: 스트리밍 루프 안에서 무거운 JPA `save()`를 호출했을 때의 현상
- Netty 워커 스레드가 블로킹되어 전체 서버가 멈추는 원리
- **해결책**: `subscribeOn(Schedulers.boundedElastic())`의 오프로딩(Offloading) 메커니즘 분석
