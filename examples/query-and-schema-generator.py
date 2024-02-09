#!/usr/bin/env python
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
import sqlparse as sp
import argparse as ap
import re
from enum import Enum
from lark import Lark, Transformer

provtable = "RPrime"

dl_parser = Lark('''
start: rule+

rule: headatom DLIMP body "."

body: atom ("," atom)*

atom: relatom | compatom

headatom: var "(" expr_list? ")"

expr_list: expr ("," expr)*

relatom: var "(" arg ("," arg)* ")"

compatom: expr COMPOP expr | "(" expr COMPOP expr ")"

expr: var | const | aexpr | pexpr | fcall

fcall: var "(" expr_list? ")"

pexpr: "(" expr ")"

aexpr: expr AOP expr

AOP: "+" | "*" | "/" | "-"

COMPOP: "<" | ">" | "!=" | "=" | "<=" | ">="

var: CNAME

arg: var | const

const: NUMBER | STRCONST

STRCONST: /["][^"]*["]/ | /['][^']*[']/
NUMBER: /[0-9]+([.][0-9]*)?/
DLIMP: ":-"

// imports WORD from library
%import common.CNAME
%import common.WS

// Disregard spaces in text
%ignore WS
''')

class DLTransformer(Transformer):

    def start(self, args):
        return args

    def rule(self,args):
        head = args[0]
        body = args[2]
        return dlrule(head, body)

    def headatom(self,args):
        relname = args[0]
        aargs = args[1]
        return atom(relname,aargs)

    def body(self,args):
        return args

    def expr_list(self,args):
        return args

    def atom(self,args):
        return args[0]

    def relatom(self,args):
        relname = args[0]
        aargs = args[1:]
        return atom(relname, aargs)

    def compatom(self,args):
        return Operator(args[1],[args[0], args[2]])

    def expr(self,args):
        return args[0]

    def fcall(self,args): #TODO
        fname = args[0]
        fargs = list(args[1:])
        return FunctionCall(fname, fargs) # [fname] + fargs

    def pexpr(self,args):
        return args[0] #TODO

    def aexpr(self,args):
        return Operator(args[1],[args[0], args[2]])
        # return args #TODO

    AOP = str
    COMPOP = str
    WORD = str

    def var(self,args):
        return args[0]

    def arg(self,args):
        #print(f"[[arg]]: args: {args}, return {args[0]}")
        return args[0]

    def const(self,args):
        return args[0]

    STRCONST = str
    NUMBER = str
    DLIMP = str

datatype_to_java = {
    'INT8' : 'java.lang.Integer',
    'NUMBER' : 'java.lang.Integer',
    'NUMERIC' : 'java.lang.Double',
    'VARCHAR' : 'java.lang.String',
    "DATE" : 'java.util.Date'
    }

sql_dt_replacements = {
    r'DOUBLE': 'NUMERIC',
    r'VARCHAR([(][0-9]+[)])?': 'VARCHAR'
}

access_method_counter = 1

userResultTable = 'RPrime'	# table that holds user selected rows

tpchTest = 'dlrule(head("Q",["x1","x2","y1","y2"]),[atom("customer",["x1","x2","x3","y1","x5","x6","x7","x8"]),atom("nation",["y1","y2","y3","y4"])])'
pListTest = ["customer","nation"]

tpchQ18 = 'dlrule(head("Q",["x2","x1","y1","y5","y4","a1"]),[atom("customer",["x1","x2","x3","x4","x5","x6","x7","x8"]),atom("orders",["y1","x1","y3","y4","y5","y6","y7","y8","y9"]),atom("lineitem",["y1","z2","z3","z4","z5","z6","z7","z8","z9","z10","z11","z12","z13","z14","z15","z16",]),atom("tempTab",["y1","a1"])])'
pListQ18 = ["customer","orders","lineitem"]

query = tpchQ18
pList = pListQ18

#tpcq18_Q = 'dlrule(head("Q",["x","y"]),[atom("R",["x","y"])])'

class IncludePKs(Enum):
    NO = 0
    AS_PK = 1
    AS_EGD = 2

@dataclass
class Table:
    name: str
    attrs: List[Tuple[str]]
    pk: Optional[List[str]]

    def attrNames(self):
        return [ x[0] for x in self.attrs ]

    def pkToFD(self):
        if self.pk:
            return FD(self, self.pk, self.attrNames())
        return None

    def pkToXML(self):
        if self.pk:
            return "\n" + "\n".join([ f"             <primaryKey>{a}</primaryKey>" for a in self.pk ])
        else:
            return ""

    def toXML(self,addPK=False):
        global access_method_counter
        access_method_counter += 1
        accm = f'm{access_method_counter}'
        attrStr = listConcat([ "             " + attrToXML(x[0], x[1]) for x in self.attrs])
        pkStr = ""
        if (addPK):
            pkStr = self.pkToXML()
        if self.name == provtable:
                    return f"""     <relation name="{self.name}">
{attrStr}{pkStr}
             <access-method name="{accm}" type="LIMITED"/>
    </relation>
    """
        return f"""     <relation name="{self.name}">
{attrStr}{pkStr}
             <access-method name="{accm}"/>
     </relation>
"""

    def viewXML(self):
        attrStr = listConcat([ "             " + attrToXML(x[0], x[1]) for x in self.attrs])
        return f"""<view name="{'v' + self.name}">
{attrStr}
\t</view>
"""

@dataclass
class View:
    name: str
    attrs: List[Tuple[str]]

    def toXML(self):
        attrStr = listConcat([ "             " + attrToXML(x[0], x[1]) for x in self.attrs])
        return f"""<view name="{self.name}">
{attrStr}
\t</view>
"""


@dataclass
class Counter:
    val: int = 0

    def incr(self):
        self.val += 1
        return self.val - 1

@dataclass
class FD:
    table: Table
    lhs: List[str]
    rhs: List[str]

    @staticmethod
    def genVar(a, lhsToVar, rhsToVar, c):
        if a in lhsToVar:
            return lhsToVar[a]
        if a in rhsToVar:
            return rhsToVar[a]
        return varFromInt(c.incr())

    def toEDG(self):
        c = Counter()
        lhsToVar = { a: varFromInt(c.incr()) for a in self.lhs }
        onlyRhs = list(filter(lambda x: x not in self.lhs, self.rhs))
        rhsToVarLeft = { a: varFromInt(c.incr()) for a in onlyRhs }
        rhsToVarRight = { a: varFromInt(c.incr()) for a in onlyRhs }

        fa = atom(self.table.name, [ FD.genVar(a, lhsToVar, rhsToVarLeft, c) for a in self.table.attrNames()])
        sa = atom(self.table.name, [ FD.genVar(a, lhsToVar, rhsToVarRight, c) for a in self.table.attrNames()])
        eqs = [ (rhsToVarLeft[a], rhsToVarRight[a]) for a in onlyRhs ]

        return EDG([fa,sa], eqs)

    def toXML(self):
        return self.toEDG().toXML()

@dataclass
class Expr:
    def toXML(self, indent=3):
        return ""

@dataclass
class Operator(Expr):
    name: str
    args: List[Expr]
    def toXML(self, indent=3):
        return ""

@dataclass
class FunctionCall(Expr):
    name: str
    args: List[Expr]
    def toXML(self, indent=3):
        return ""

@dataclass
class Const(Expr):
    val: str
    def toXML(self, indent=3):
        return ""

@dataclass
class Var(Expr):
    name: str
    def toXML(self, indent=3):
        return ""

@dataclass
class atom:
    name: str
    args: List[str]

    def toXML(self, indent=3):
        varStrs = listConcat([ '\t' + varToXML(arg) for arg in self.args])
        return listConcat([ indent * '\t' + s for s in  f"""<atom name="{self.name}">
{varStrs}
</atom>""".split("\n")])

class Dependency:
    pass

@dataclass
class TGD(Dependency):
    lhs: List[atom]
    rhs: List[atom]

    def toXML(self):
        return f"""<dependency type="TGD">
      <body>
{listConcat([ a.toXML() for a in self.lhs])}
      </body>
      <head>
{listConcat([ a.toXML() for a in self.rhs])}
      </head>
    </dependency>
"""

@dataclass
class EDG(Dependency):
    lhs: List[atom]
    rhs: List[Tuple[str,str]]

    @staticmethod
    def equalityToXML(eq):
        return f"""
            <atom name="EQUALITY">
                <variable name="{eq[0]}" />
                <variable name="{eq[1]}" />
            </atom>
"""

    def toXMLSingle(self):
        return f"""    <dependency>
	  <body>
{listConcat([ a.toXML() for a in self.lhs])}
	  </body>
	  <head>
{listConcat([ EDG.equalityToXML(e) for e in self.rhs])}
	  </head>
    </dependency>
"""

    def toXML(self):
        rhsSplit = []
        count = 0
        while (count < len(self.rhs)):
            if (count % 3 == 0):
                rhsSplit.append([])
            rhsSplit[count // 3].append(self.rhs[count])
            count += 1
        s = ""
        for x in rhsSplit:
            s += f"""    <dependency>
        <body>
{listConcat([ a.toXML() for a in self.lhs])}
	  </body>
	  <head>
{listConcat([ EDG.equalityToXML(e) for e in x])}
	  </head>
    </dependency>
"""
        return s



@dataclass
class Schema:
    tables: List[Table]
    deps: List[Dependency]
    views: List[View]

    def toXML(self, pk=IncludePKs.NO):
        strtable = [ t.toXML(pk is IncludePKs.AS_PK) for t in self.tables ]
#        strviews = [ t.viewXML() for t in self.tables ] + [ v.toXML() for v in self.views ]
#        strdeps = [ createTableViewDep(t) for t in self.tables ]
        strviews = []
        strdeps = []
        if pk is IncludePKs.AS_EGD:
            '''for t in self.tables:
                if t.pk:
                    print(depToXML(t.pkToFD().toEDG())) '''
            strdeps += [ depToXML(t.pkToFD().toEDG()) for t in self.tables if t.pk ]
        strdeps += [ depToXML(d) for d in self.deps ]

        return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<schema>
<relations>
{listConcat(strtable)}
{listConcat(strviews)}
</relations>
<dependencies>
{listConcat(strdeps)}
</dependencies>
</schema>
"""



def replace_non_sqlparse_dts(s):
    for (rex, repl) in sql_dt_replacements.items():
        s = re.sub(rex, repl, s)
    return s

def dtToJava(dt):
    global datatype_to_java
    return datatype_to_java[dt]

def attrToXML(name, dt):
    return f'<attribute name="{name}" type="{dtToJava(dt)}"/>'

def varFromInt(i, basename='x'):
    return f"x{i}"

def varToXML(name):
    return f'<variable name="{name}" />'

def createVarList(attrs, tabs):
    return listConcat([ (tabs * '\t') + varToXML(varFromInt(i)) for i in range(0,len(attrs)) ])

def createTableViewDep(table):
    return f"""
    <dependency type="TGD">
      <body>
        <atom name="{table.name}">
{createVarList(table.attrs, 3)}
        </atom>
      </body>
      <head>
        <atom name="{'v' + table.name}">
{createVarList(table.attrs, 3)}
        </atom>
      </head>
    </dependency>
    <dependency type="TGD">
      <body>
        <atom name="{'v' + table.name}">
{createVarList(table.attrs, 3)}
        </atom>
      </body>
      <head>
        <atom name="{table.name}">
{createVarList(table.attrs, 3)}
        </atom>
      </head>
    </dependency>
"""

def listConcat(strl, delim='\n'):
    return delim.join(strl)

def depToXML(d):
    return d.toXML()

def firstToken(lst, cls):
    return next(iter([ t for t in lst if  isinstance(t, cls)]))

def splitOnComma(ts):
    elements = []
    cur = []
    for t in ts:
        if t.ttype == sp.tokens.Punctuation and t.value == ',':
            elements.append(cur)
            cur = []
        else:
            cur.append(t)
    elements.append(cur)
    return elements

def extractAttrs(ts):
    attrs = []
    for el in ts:
        if isinstance(el[0], sp.sql.Identifier):
            attrs.append((el[0].value, el[1].value))
# TODO check for primary key
    return attrs

def sqlkeyToKey(tok):
    if isinstance(tok, sp.sql.Identifier):
        return [ tok.value ]
    else:
        return [ t.value for t in tok.get_identifiers() ]

def extractPK(ts):
    for el in ts:
        if el[0].value.lower() == 'primary' and el[1].value.lower() == 'key':
            els = splitOnComma([ t for t in firstToken(el, sp.sql.Parenthesis) if not t.is_whitespace ][1:-1])
            return sqlkeyToKey(els[0][0])
    return None

def sqlCreateTableParseToTable(st):
    nonwhite = [ t for t in st.tokens if not t.is_whitespace ]
    tableName = firstToken(nonwhite, sp.sql.Identifier).value
    els = [ t for t in firstToken(nonwhite, sp.sql.Parenthesis) if not t.is_whitespace ][1:-1]
    els = splitOnComma(els)
    attrs = extractAttrs(els)
    pk = extractPK(els)

    return Table(tableName, attrs, pk)

def sqlToSchema(f, replaceDTs=True):
    tables = [ ]
    with open (f, "r") as sqlfile:
        content = " ".join(sqlfile.readlines()).replace("\t", " ").replace("\n", " ")
    if replaceDTs:
        content = replace_non_sqlparse_dts(content)
    parse = sp.parse(content)
    for st in parse:
        if st.get_type() == 'CREATE':
            table = sqlCreateTableParseToTable(st)
            tables.append(table)
    return Schema(tables, [], [])

def writeXMLForSchema(sch, f, pk=IncludePKs.NO):
    with open (f, 'w') as xmlschema:
        xmlschema.write(sch.toXML(pk))

def writeXMLForQuery(rule, f):
    with open (f, 'w') as qfile:
        qfile.write(rule.toXML())

@dataclass
class head(atom):
    def toXML(self, indent=1):
        varStrs = listConcat([ '\t' + varToXML(arg) for arg in self.args])
        return listConcat([ indent * '\t' + s for s in  f"""<head name="{self.name}">
{varStrs}
</head>""".split("\n")])

@dataclass
class dlrule():
    head: head
    body: List[atom]

    def toXML(self):
        strbody = [x.toXML() for x in self.body]
        strhead = self.head.toXML()
        return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<query>
  <body>
    {listConcat(strbody)}
  </body>
  {strhead}
</query>
"""
    def getViewName(self):
        return f"qv_{self.head.name}"

    def getViewDef(self):
        return View(self.getViewName(),
                [ (x, "VARCHAR") for x in self.head.args ]) #TODO actually take DTs

    def getResultTableDef(self):
        return Table(userResultTable,
                [ (x, "VARCHAR") for x in self.head.args ], None) #TODO actually take DTs

    def toViewXML(self): #TODO need data types from schema
        attrStr = listConcat([ "             " + attrToXML(x[0], "VARCHAR") for x in self.head.args])
        return f"""<view name="{self.getViewName()}">
        {attrStr}
    </view>
"""

    def getICasTGD(self):
        rhs = atom(self.getViewName(), self.head.args)
        t = TGD(self.body, [rhs])
        return t


    def getICasTGDReverse(self):
        rhs = atom(self.getViewName(), self.head.args)
        t = TGD([rhs],self.body)
        return t


    def toICXML(self):
        rhs = atom(self.getViewName(), self.head.args)
        t = TGD(self.body, [rhs])
        return t.toXML()

def addQueryConstraintsToSchema(schema, rule):
    schema.views += [rule.getViewDef()]
    schema.deps += [rule.getICasTGD()]
    schema.deps += [rule.getICasTGDReverse()]

def addResultTableToSchema(schema, rule):
    schema.tables += [rule.getResultTableDef()]
    #add TGD RPrime to the original query body
    resultTGD = rule.getICasTGDReverse()
    resultTGD.lhs[0].name = userResultTable
    schema.deps += [resultTGD]

def translateSQLtoXMLfile(conf):
    pk=IncludePKs.NO
    if conf.p:
        pk=IncludePKs.AS_PK
    elif conf.f:
        pk=IncludePKs.AS_EGD
    schema = sqlToSchema(conf.infile, conf.replace_dt)
    if conf.outfile:
        writeXMLForSchema(schema, conf.outfile, pk=pk)
    else:
        print(schema.toXML(pk))

def translateSQLandQueryToXMLfile(conf):
    pk=IncludePKs.NO
    if conf.p:
        pk=IncludePKs.AS_PK
    elif conf.f:
        pk=IncludePKs.AS_EGD
    schema = sqlToSchema(conf.infile, conf.replace_dt)
    rule = conf.query
#    addQueryConstraintsToSchema(schema,rule)
    print(schema.toXML())
    if conf.outfile:
        writeXMLForSchema(schema, conf.outfile, pk=pk)
    else:
        print(schema.toXML(pk))
    if conf.query_file:
        writeXMLForQuery(rule, conf.query_file)

# Q(X) :- R(X,Y), S(Y,Z). tracking provenance for R, then we need to write R(X,Y) :- R(X,Y), S(Y,Z), Rprime(X) where Rprime <= Q
# changing targetAtom to head -- MM
def provRewriteQuery(conf,rule):
    targetAtom = [ a for a in rule.body if a.name == conf.prov ][0]
    rprime = atom(userResultTable, rule.head.args)
    targetAtom = head("prov_" + targetAtom.name, targetAtom.args)
    return dlrule(targetAtom, rule.body + [rprime])

def translateSQLandQueryandProvToXMLfile(conf):
    pk=IncludePKs.NO
    if conf.p:
        pk=IncludePKs.AS_PK
    elif conf.f:
        pk=IncludePKs.AS_EGD
    schema = sqlToSchema(conf.infile, conf.replace_dt)
    rule = conf.query
    print(rule.head.name)
#    addQueryConstraintsToSchema(schema,rule)
    addResultTableToSchema(schema,rule)			# add table for the user selected rows
    print(f"{schema}")
    print(schema.toXML())
    if conf.outfile:
        writeXMLForSchema(schema, conf.outfile, pk=pk)
    else:
        print(schema.toXML(pk))
    if conf.query_file:
        rewrule = provRewriteQuery(conf, rule)
        writeXMLForQuery(rewrule, conf.query_file)

def test():
    pk=IncludePKs.AS_EGD
    #infile = 'tpch/tpch.sql'
    infile = 'tpchQ18/tpch.sql'
    schema = sqlToSchema(infile, True)
    rule = eval(query.strip())
    #print(rule.head.name)
    addQueryConstraintsToSchema(schema,rule)
    addResultTableToSchema(schema,rule)			# add table for the user selected rows
    print(schema.toXML())
    outfile = 'tpchQ18/testS.xml'
    writeXMLForSchema(schema, outfile, pk=pk)
    query_file = 'tpchQ18/testQ.xml'
    prov = pList[0]

    temp = [a.name for a in rule.body]
    print(', '.join(temp))
    targetAtom = [ a for a in rule.body if a.name == prov ][0]
    rprime = atom(userResultTable, rule.head.args)
    targetAtom = head("prov_" + targetAtom.name, targetAtom.args)
    rewrule = dlrule(targetAtom, rule.body + [rprime])
    writeXMLForQuery(rewrule, query_file)

def parse_dl_query(q):
    dlt = DLTransformer()
    p = dl_parser.parse(q)
    return dlt.transform(p)[0]

def main():
    parser = ap.ArgumentParser(description='Create PDQs XML schemas.')
    subparsers = parser.add_subparsers()


    # translate SQL to XML
    sqltoxml_parser = subparsers.add_parser('translate_sql')
    sqltoxml_parser.add_argument("-i", "--infile", type=str, help='input SQL file (no newlines because of limitation of sqlparse library', required=True)
    sqltoxml_parser.add_argument("-P", action='store_true', help='parse query instead of eval it', default=False)
    sqltoxml_parser.add_argument("-o", "--outfile", type=str, help='output XML file. If no file is provided write to stdout', required=False)
    sqltoxml_parser.add_argument("-p", action='store_true', help='output primary key constraints in relation elements')
    sqltoxml_parser.add_argument("-f", action='store_true', help='output PK constraints as EGDs')
    sqltoxml_parser.add_argument("--replace_dt", action='store_true', help='replace DTs not support by python sqlparse')
    sqltoxml_parser.set_defaults(func=translateSQLtoXMLfile)

    # translate query and SQL schema to XML
    sqlqtoxml_parser = subparsers.add_parser('translate_sql_and_query')
    sqlqtoxml_parser.add_argument("-i", "--infile", type=str, help='input SQL file (no newlines because of limitation of sqlparse library', required=True)
    sqlqtoxml_parser.add_argument("-P", action='store_true', help='parse query instead of eval it', default=False)
    sqlqtoxml_parser.add_argument("-o", "--outfile", type=str, help='output XML file. If no file is provided write to stdout', required=False)
    sqlqtoxml_parser.add_argument("--query_file", type=str, help='name of XML file output file for the query', required=False)
    sqlqtoxml_parser.add_argument("-q", "--query", type=str, help='query as a python dlrule object', required=True)
    sqlqtoxml_parser.add_argument("-p", action='store_true', help='output primary key constraints in relation elements')
    sqlqtoxml_parser.add_argument("-f", action='store_true', help='output PK constraints as EGDs')
    sqlqtoxml_parser.add_argument("--replace_dt", action='store_true', help='replace DTs not support by python sqlparse')
    sqlqtoxml_parser.set_defaults(func=translateSQLandQueryToXMLfile)

    # translate provenance, query and SQL schema to XML
    sqlqtoxml_parser = subparsers.add_parser('translate_sql_and_query_and_prov')
    sqlqtoxml_parser.add_argument("-P", action='store_true', help='parse query instead of eval it', default=False)
    sqlqtoxml_parser.add_argument("-i", "--infile", type=str, help='input SQL file (no newlines because of limitation of sqlparse library', required=True)
    sqlqtoxml_parser.add_argument("-o", "--outfile", type=str, help='output XML file. If no file is provided write to stdout', required=False)
    sqlqtoxml_parser.add_argument("--query_file", type=str, help='name of XML file output file for the query', required=False)
    sqlqtoxml_parser.add_argument("-q", "--query", type=str, help='query as a python dlrule object', required=True)
    sqlqtoxml_parser.add_argument("--prov", type=str, help='name of prov table', required=True)
    sqlqtoxml_parser.add_argument("-p", action='store_true', help='output primary key constraints in relation elements')
    sqlqtoxml_parser.add_argument("-f", action='store_true', help='output PK constraints as EGDs')
    sqlqtoxml_parser.add_argument("--replace_dt", action='store_true', help='replace DTs not support by python sqlparse')
    sqlqtoxml_parser.set_defaults(func=translateSQLandQueryandProvToXMLfile)

    # call function for subcommand
    conf = parser.parse_args()
    if isinstance(conf.query, str):
        conf.query = parse_dl_query(conf.query)
    else:
        conf.query = eval(conf.query.strip())
    conf.func(conf)

def test1():
        rhs = [(1,11)]
        rhsSplit = []
        count = 0
        while (count < len(rhs)):
            if (count // 3 == 0):
                rhsSplit.append([])
            rhsSplit[count // 3].append(rhs[count])
            count += 1
        s = ""
        for x in rhsSplit:
            s += f"""    <dependency>
        <body>lhs</body>
	  <head>
{listConcat([ EDG.equalityToXML(e) for e in x])}
	  </head>
    </dependency>
"""
        print(s)

if __name__ == '__main__':
    main()
    #test()
