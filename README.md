# ClothesDay
서일대학교 캡스톤디자인 - 날씨별 옷차림 커뮤니티 앱(옷날)

Android(JAVA)    
JSP    
Apache Tomcat 8.5    
MariaDB

# DB 


    
### MEMBER(회원 테이블)
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

### LOGIN(로그인 테이블)
|NO|컬럼ID|컬럼명|타입|길이|NULL|KEY|DEFAULT|비고|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|1|LOG_ID|로그인번호|int|11|N|PK||AUTO_INCREMENT|
|2|LOG_ME_ID|회원ID|varchar|30|N|FK||MEMBER.ME_ID|
|3|LOG_DA|로그인일시|datetime||N||CURRENT_TIMESTAMP||

### FOLLOW(팔로우 테이블)
|NO|컬럼ID|컬럼명|타입|길이|NULL|KEY|DEFAULT|비고|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|1|FO_ME_ID|회원ID|varchar|30|N|PK,FK||MEMBER.ME_ID|
|2|FO_FOL_ID|팔로워|varchar|30|N|PK,FK||MEMBER.ME_ID|
|3|FO_DA|팔로우일시|datetime||||CURRENT_TIMESTAMP||


### POST(게시글 테이블)
|NO|컬럼ID|컬럼명|타입|길이|NULL|KEY|DEFAULT|비고|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|1|PO_ID|게시글번호|int|11|N|PK||AUTO_INCREMENT|
|2|PO_CON|게시글내용|varchar|250|||||
|3|PO_ME_ID|회원ID|varchar|30|N|FK||MEMBER.ME_ID|
|4|PO_REG_DA|게시글작성일시|datetime||N||CURRENT_TIMESTAMP||
|5|PO_CHA_DA|게시글수정일시|datetime||||||
|6|PO_TAG|게시글태그|varchar|20|||||
|7|PO_CATE|게시글카테고리|char|10|N||||
|8|PO_PIC|첨부파일경로|varchar|300|||||
|9|PO_LIKE|좋아요수|int|11|N||0||

### POSTSCRAP(게시글 스크랩 테이블)
|NO|컬럼ID|컬럼명|타입|길이|NULL|KEY|DEFAULT|비고|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|1|PS_ID|스크랩번호|int|11|N|PK||AUTO_INCREMENT|
|2|PS_PO_ID|게시글번호|int|11||FK||POST.PO_ID|
|3|PS_DATE|스크랩일시|datetime||||CURRENT_TIMESTAMP||
|4|PS_ME_ID|회원ID|varchar|30|N|FK||MEMBER.ME_ID|

### REPLY(게시글 댓글 테이블)
|NO|컬럼ID|컬럼명|타입|길이|NULL|KEY|DEFAULT|비고|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|1|RE_ID|댓글번호|int|11|N|PK||AUTO_INCREMENT|
|2|RE_CON|댓글내용|varchar|80|N||||
|3|RE_ME_ID|회원ID|varchar|30||FK||MEMBER.ME_ID|
|4|RE_REG_DA|댓글작성일시|datetime||N||CURRENT_TIMESTAMP||
|5|RE_CHA_DA|댓글수정일시|datetime||||||
|6|RE_PO_ID|게시글번호|int|11|N|FK||POST.PO_ID|

### REPORT(게시글 신고 테이블)
|NO|컬럼ID|컬럼명|타입|길이|NULL|KEY|DEFAULT|비고|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|1|RP_ID|신고번호|int|11|N|PK||AUTO_INCREMENT|
|2|RP_VIL_ID|신고대상|varchar|30|N|FK||MEMBER.ME_ID|
|3|RP_ME_ID|신고자|varchar|30|N|FK||MEMBER.ME_ID|
|4|RP_CON|신고내용|varchar|100|N||||
|5|RP_DA|신고일시|datetime||||CURRENT_TIMESTAMP||
|6|RP_PO_ID|신고게시글번호|int|11|N|FK||POST.PO_ID|

### SEARCH(검색 테이블)
|NO|컬럼ID|컬럼명|타입|길이|NULL|KEY|DEFAULT|비고|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|1|SE_ID|검색번호|int|11|N|PK||AUTO_INCREMENT|
|2|SE_CON|검색내용|char|30|N||||
|3|SE_DA|검색일시|datetime||N||CURRENT_TIMESTAMP||
|4|SE_ME_ID|회원ID|varchar|30|N|FK||MEMBER.ME_ID|

### LIKETABLE(게시글 좋아요 테이블)
|NO|컬럼ID|컬럼명|타입|길이|NULL|KEY|DEFAULT|비고|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|1|LIKE_ME_ID|회원ID|varchar|30|N|PK,FK||MEMBER.ME_ID|
|2|LIKE_PO_ID|게시글번호|int|11|N|PK,FK||POST.PO_ID|
|3|LIKE_DA|좋아요일시|datetime||N||CURRENT_TIMESTAMP||



