  0         LOADL        0
  1         CALL         newarr  
  2         CALL         L10
  3         HALT   (0)   
  4  L10:   LOADL        1
  5         CALL         neg     
  6         LOADL        0
  7  L11:   LOADA        4[LB]
  8         LOADI  
  9         LOADL        4
 10         CALL         lt      
 11         JUMPIF (0)   L12
 12         LOADA        4[LB]
 13         LOADA        4[LB]
 14         LOADI  
 15         LOADL        1
 16         CALL         add     
 17         STORE        4[LB]
 18         LOADA        3[LB]
 19         LOADA        4[LB]
 20         LOADI  
 21         STORE        3[LB]
 22         JUMP         L11
 23  L12:   LOADA        3[LB]
 24         LOADI  
 25         CALL         putintnl
 26         RETURN (0)   1
