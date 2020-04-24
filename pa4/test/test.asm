  0         LOADL        0
  1         CALL         newarr  
  2         CALL         L10
  3         HALT   (0)   
  4  L10:   LOADL        2
  5         LOADL        1
  6         CALL         mult    
  7         CALL         newarr  
  8         STORE        0[LB]
  9         LOAD         3[LB]
 10         CALL         putintnl
 11         RETURN (0)   1
