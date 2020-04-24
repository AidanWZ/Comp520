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
 14         LOADL        5
 15         LOAD         3[LB]
 16         CALL         add     
 17         STOREI 
 18         LOADL        1
 19         CALL         add     
 20         LOAD         3[LB]
 21         CALL         add     
 22         LOADL        1
 23         STORE        3[LB]
 24         LOAD         3[LB]
 25         CALL         add     
 26         LOADI  
 27         LOADL        1
 28         CALL         add     
 29         LOAD         3[LB]
 30         CALL         add     
 31         LOADI  
 32         CALL         add     
 33         LOAD         4[LB]
 34         LOADI  
 35         CALL         putintnl
 36         RETURN (0)   1
