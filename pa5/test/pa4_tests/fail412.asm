  0         LOADL        0
  1         CALL         newarr  
  2         CALL         L10
  3         HALT   (0)   
  4  L10:   LOADL        5
  5         LOAD         0[LB]
  6         CALLI        L11
  7         STORE        0[LB]
  8         RETURN (0)   1
  9  L11:   LOAD         -1[LB]
 10         LOADL        5
 11         CALL         ne      
 12         JUMPIF (0)   L12
 13         POP          0
 14         LOADL        0
 15  L12:   LOADL        5
 16         STORE        -1[LB]
