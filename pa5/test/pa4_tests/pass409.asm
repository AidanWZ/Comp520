  0         LOADL        0
  1         CALL         newarr  
  2         CALL         L10
  3         HALT   (0)   
  4  L10:   LOADL        4
  5         LOADA        3[LB]
  6         LOADI  
  7         CALL         newarr  
  8         LOADL        1
  9         LOADA        4[LB]
 10         LOADA        5[LB]
 11         LOADI  
 12         LOADL        0
 13         LOAD         4[LB]
 14         CALL         add     
 15         STOREI 
 16  L11:   LOADA        5[LB]
 17         LOADI  
 18         CALL         arraylen
 19         CALL         lt      
 20         JUMPIF (0)   L12
 21         LOADA        4[LB]
 22         LOADA        4[LB]
 23         LOAD         4[LB]
 24         LOADA        5[LB]
 25         LOADI  
 26         LOADL        1
 27         CALL         sub     
 28         CALL         add     
 29         LOADI  
 30         LOADA        5[LB]
 31         LOADI  
 32         CALL         add     
 33         LOADA        5[LB]
 34         LOADI  
 35         LOAD         4[LB]
 36         CALL         add     
 37         STOREI 
 38         LOADA        5[LB]
 39         LOADA        5[LB]
 40         LOADI  
 41         LOADL        1
 42         CALL         add     
 43         STORE        5[LB]
 44         JUMP         L11
 45  L12:   LOADA        4[LB]
 46         LOAD         4[LB]
 47         LOADL        3
 48         CALL         add     
 49         LOADI  
 50         LOADL        2
 51         CALL         add     
 52         LOADA        6[LB]
 53         LOADI  
 54         CALL         putintnl
 55         RETURN (0)   1
