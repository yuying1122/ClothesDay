# ClothesDay
서일대학교 캡스톤디자인 - 날씨별 옷차림 커뮤니티 앱(옷날)

Android(JAVA)    
JSP    
Apache Tomcat 8.5    
MariaDB

# DB 

# MEMBER(회원 테이블)
|NO|컬럼ID|컬럼명|타입|길이|NULL|KEY|DEFAULT|비고|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|1|ME_ID|회원ID|varchar|30|N|PK|'0'|UNIQUE|
|2|ME_NICK|닉네임|char|10|N|||UNIQUE|
|3|ME_CD|회원유형코드|char|3|||'일반'||
|4|ME_PW|비밀번호|varchar|15|N||||
|5|ME_REG_DA|회원가입일시|datetime||||CURRENT_TIMESTAMP||
|6|ME_PIC|프로필사진경로|varchar|50|||||
|7|ME_TEXT|프로필소개|varchar|50|||||
|8|ME_TOKEN|FCM토큰|varchar|200|||||
