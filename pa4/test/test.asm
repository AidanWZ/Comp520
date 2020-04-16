  0         LOADL        0
  1         CALL         newarr  
  2         CALL         L10
  3         HALT   (0)   
  4  L10:   LOADL        23
  5         CALL         newarr  
  6         LOAD         3[LB]
  7         CALL         arraylen
  8         CALL         putintnl
  9         RETURN (0)   1
