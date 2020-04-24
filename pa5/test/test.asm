  0         LOADL        0
  1         CALL         newarr  
  2         CALL         L10
  3         HALT   (0)   
  4  L10:   LOADL        -1
  5         LOADL        2
  6         CALL         newobj  
  7         LOADL        -1
  8         LOADL        2
  9         CALL         newobj  
 10         LOADL        1
 11         LOAD         3[LB]
 12         CALL         add     
 13         STOREI 
 14         LOADL        1
 15         CALL         add     
 16         LOAD         3[LB]
 17         CALL         add     
 18         LOADL        1
 19         STORE        3[LB]
 20         LOADL        1
 21         CALL         add     
 22         LOAD         3[LB]
 23         CALL         add     
 24         LOADI  
 25         CALL         putintnl
 26         RETURN (0)   1
