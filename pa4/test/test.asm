  0         LOADL        0
  1         CALL         newarr  
  2         CALL         L10
  3         HALT   (0)   
  4  L10:   LOADL        2
  5         CALL         newarr  
  6         LOADA        0[HT]
  7         STOREI 
  8         LOADA        -1[HB]
  9         LOADL        3
 10         LOADL        0
 11         CALL         add     
 12         STOREI 
 13         LOAD         -1[HB]
 14         LOADL        0
 15         CALL         add     
 16         LOADI  
 17         LOADL        1
 18         CALL         ne      
 19         JUMPIF (0)   L11
 20         LOAD         -1[HB]
 21         LOADL        0
 22         CALL         add     
 23         LOADI  
 24         LOAD         0[LB]
 25         CALLI        L12
 26  L11:   LOADL        20
 27         CALL         putintnl
 28         RETURN (0)   1
 29  L12:   LOAD         -1[LB]
 30         CALL         putintnl
 31         RETURN (0)   1
