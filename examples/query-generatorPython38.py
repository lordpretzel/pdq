'''
#Q(a, b, c) :- R1 (x, y, z1, x1), R2 (...), ...
#Q(a, b, c), R1(x, y, z1, x1), ...
# [str, [str]]
# [Q, [a, b, c], R1, [x, y, z1, x1]]
# (or)
# str, [str], [str, [str]]
# Q, [a, b, c], [R1, [x, y, z1, x1]]
# head, [atom]
'''

from functools import reduce
import operator
from dataclasses import dataclass
from typing import Optional, List, Tuple
import argparse as ap
import re
from enum import Enum

@dataclass
class atom:
    name: str
    args: List[str]

    def toXML(self, indent=2):
        varStrs = listConcat([ '\t' + varToXML(arg) for arg in self.args])
        return listConcat([ indent * '\t' + s for s in  f"""<atom name="{self.name}">
{varStrs}
</atom>""".split("\n")])

@dataclass
class head(atom):
    def toXML(self, indent=1):
        varStrs = listConcat([ '\t' + varToXML(arg) for arg in self.args])
        return listConcat([ indent * '\t' + s for s in  f"""<head name="{self.name}">
{varStrs}
</head>""".split("\n")])

def varToXML(name):
    return f'<variable name="{name}" />'

def listConcat(strl, delim='\n'):
    return delim.join(strl)

def queryToXML(h, atoms):
    strbody = [x.toXML() for x in atoms]
    strhead = h.toXML()
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<query>
  <body>
    {listConcat(strbody)}
  </body>
  {strhead}
</query>
"""


x = head("Q", ["a", "b"])
y1 = atom("R1", ["x1", "y1"])
y2 = atom("R2", ["y1", "a", "b"])
y = []
y.append(y1)
y.append(y2)
print(queryToXML(x, y))
