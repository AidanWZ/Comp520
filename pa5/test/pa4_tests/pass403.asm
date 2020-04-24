  0         LOADL        0
  1         CALL         newarr  
  2         CALL         L10
  3         HALT   (0)   
  4  L10:   LOADL        2
  5         LOADA        3[LB]
  6         LOADI  
  7         LOADL        2
  8         CALL         eq      
  9         JUMPIF (0)   L11
 10         LOADA        3[LB]
 11         LOADL        3
 12         STORE        3[LB]
 13         JUMP         ***
 14  L11:   LOADA        3[LB]
 15         LOADL        1
 16         CALL         neg     
 17         STORE        3[LB]
 18  L12:   LOADA        3[LB]
 19         LOADI  
 20         CALL         putintnl
 21         RETURN (0)   1
