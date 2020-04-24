  0         LOADL        0
  1         CALL         newarr  
  2         CALL         L10
  3         HALT   (0)   
  4  L10:   LOADL        1
  5         LOADA        3[LB]
  6         LOADI  
  7         CALL         putintnl
  8         RETURN (0)   1
