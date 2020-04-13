  0         LOADL        0
  1         CALL         newarr  
  2         CALL         L10
  3         HALT   (0)   
  4         LOAD         0[OB]
  5         LOAD         -1[LB]
  6         CALL         add     
  7         STORE        0[OB]
  8         RETURN (0)   1
  9  L10:   LOADL        1
 10         CALL         neg     
 11         LOADL        0
 12         CALL         gt      
 13         LOADL        2
 14         CALL         putintnl
 15         JUMP         L12
 16  L11:   LOADL        3
 17         CALL         putintnl
 18  L12:   RETURN (0)   1
