  0         LOADL        0
  1         CALL         newarr  
  2         CALL         L10
  3         HALT   (0)   
  4  L10:   LOADL        2
  5         CALL         newarr  
  6         LOADL        3
  7         CALL         newarr  
  8         LOADL        5
  9         LOADL        0
 10         LOAD         3[LB]
 11         CALL         add     
 12         STOREI 
 13         LOADL        6
 14         LOADL        1
 15         LOAD         3[LB]
 16         CALL         add     
 17         STOREI 
 18         LOAD         3[LB]
 19         LOADL        0
 20         CALL         add     
 21         LOADI  
 22         LOADL        1
 23         CALL         ne      
 24         JUMPIF (0)   L11
 25         LOAD         3[LB]
 26         LOADL        1
 27         CALL         add     
 28         LOADI  
 29         CALL         putintnl
 30         LOAD         4[LB]
 31         LOADL        1
 32         CALL         add     
 33         LOADI  
 34         CALL         putintnl
 35  L11:   LOADL        20
 36         CALL         putintnl
 37         RETURN (0)   1
