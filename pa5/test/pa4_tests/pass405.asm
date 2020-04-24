  0         LOADL        0
  1         CALL         newarr  
  2         CALL         L10
  3         HALT   (0)   
  4  L10:   LOADL        -1
  5         LOADL        1
  6         CALL         newobj  
  7         LOADL        5
  8         LOADA        0[LB]
  9         CALL         add     
 10         LOADI  
 11         CALL         add     
 12         LOADA        4[LB]
 13         LOADI  
 14         CALL         putintnl
 15         RETURN (0)   1
