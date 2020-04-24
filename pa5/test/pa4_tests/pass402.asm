  0         LOADL        0
  1         CALL         newarr  
  2         CALL         L10
  3         HALT   (0)   
  4  L10:   LOADL        1
  5         LOADA        3[LB]
  6         LOADI  
  7         LOADL        2
  8         LOADA        3[LB]
  9         LOADI  
 10         LOADL        1
 11         CALL         sub     
 12         CALL         mult    
 13         CALL         add     
 14         LOADA        4[LB]
 15         LOADI  
 16         CALL         putintnl
 17         RETURN (0)   1
