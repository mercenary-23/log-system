# log-system

* 서버로 들어오는 HTTP 요청을 라우팅 해주고 모든 HTTP 요청과 응답에 관한 정보를 로깅하는 시스템
* 라우팅에는 RestTemplate 사용
* filter에서 로그를 찍는다
* HttpServletRequest와 HttpServletResponse의 Body 데이터는 한 번 읽으면 사라져서 ContentCachingWrapper 사용
* Logger에는 Logback 사용
* logstash-logback-encoder를 사용해서 json 형태로 로그를 찍음
* ControllerAdvice로 핸들링 할 수 있는 예외는 ResponseEntity로 응답을 보냄
* 그 외의 예외는 filter에서 try-catch 문으로 처리
* 로그 파일은 logs 폴더에 저장
