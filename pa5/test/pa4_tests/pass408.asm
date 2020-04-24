  0         LOADL        0
  1         CALL         newarr  
  2         CALL         L10
  3         HALT   (0)   
  4  L10:   LOADL        8
  5         LOADA        3[LB]
  6         LOADI  
  7         CALL         newarr  
  8         LOAD         4[LB]
  9         CALL         arraylen
 10         LOADA        5[LB]
 11         LOADI  
 12         CALL         putintnl
 13         RETURN (0)   1
