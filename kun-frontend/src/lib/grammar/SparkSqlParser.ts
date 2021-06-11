// Generated from src/lib/grammar/SparkSql.g4 by ANTLR 4.9.0-SNAPSHOT


import { ATN } from "antlr4ts/atn/ATN";
import { ATNDeserializer } from "antlr4ts/atn/ATNDeserializer";
import { FailedPredicateException } from "antlr4ts/FailedPredicateException";
import { NotNull } from "antlr4ts/Decorators";
import { NoViableAltException } from "antlr4ts/NoViableAltException";
import { Override } from "antlr4ts/Decorators";
import { Parser } from "antlr4ts/Parser";
import { ParserRuleContext } from "antlr4ts/ParserRuleContext";
import { ParserATNSimulator } from "antlr4ts/atn/ParserATNSimulator";
import { ParseTreeListener } from "antlr4ts/tree/ParseTreeListener";
import { ParseTreeVisitor } from "antlr4ts/tree/ParseTreeVisitor";
import { RecognitionException } from "antlr4ts/RecognitionException";
import { RuleContext } from "antlr4ts/RuleContext";
//import { RuleVersion } from "antlr4ts/RuleVersion";
import { TerminalNode } from "antlr4ts/tree/TerminalNode";
import { Token } from "antlr4ts/Token";
import { TokenStream } from "antlr4ts/TokenStream";
import { Vocabulary } from "antlr4ts/Vocabulary";
import { VocabularyImpl } from "antlr4ts/VocabularyImpl";

import * as Utils from "antlr4ts/misc/Utils";

import { SparkSqlListener } from "./SparkSqlListener";
import { SparkSqlVisitor } from "./SparkSqlVisitor";


export class SparkSqlParser extends Parser {
	public static readonly T__0 = 1;
	public static readonly T__1 = 2;
	public static readonly T__2 = 3;
	public static readonly T__3 = 4;
	public static readonly T__4 = 5;
	public static readonly T__5 = 6;
	public static readonly T__6 = 7;
	public static readonly T__7 = 8;
	public static readonly T__8 = 9;
	public static readonly T__9 = 10;
	public static readonly ADD = 11;
	public static readonly AFTER = 12;
	public static readonly ALL = 13;
	public static readonly ALTER = 14;
	public static readonly ANALYZE = 15;
	public static readonly AND = 16;
	public static readonly ANTI = 17;
	public static readonly ANY = 18;
	public static readonly ARCHIVE = 19;
	public static readonly ARRAY = 20;
	public static readonly AS = 21;
	public static readonly ASC = 22;
	public static readonly AT = 23;
	public static readonly AUTHORIZATION = 24;
	public static readonly BETWEEN = 25;
	public static readonly BOTH = 26;
	public static readonly BUCKET = 27;
	public static readonly BUCKETS = 28;
	public static readonly BY = 29;
	public static readonly CACHE = 30;
	public static readonly CASCADE = 31;
	public static readonly CASE = 32;
	public static readonly CAST = 33;
	public static readonly CHANGE = 34;
	public static readonly CHECK = 35;
	public static readonly CLEAR = 36;
	public static readonly CLUSTER = 37;
	public static readonly CLUSTERED = 38;
	public static readonly CODEGEN = 39;
	public static readonly COLLATE = 40;
	public static readonly COLLECTION = 41;
	public static readonly COLUMN = 42;
	public static readonly COLUMNS = 43;
	public static readonly COMMENT = 44;
	public static readonly COMMIT = 45;
	public static readonly COMPACT = 46;
	public static readonly COMPACTIONS = 47;
	public static readonly COMPUTE = 48;
	public static readonly CONCATENATE = 49;
	public static readonly CONSTRAINT = 50;
	public static readonly COST = 51;
	public static readonly CREATE = 52;
	public static readonly CROSS = 53;
	public static readonly CUBE = 54;
	public static readonly CURRENT = 55;
	public static readonly CURRENT_DATE = 56;
	public static readonly CURRENT_TIME = 57;
	public static readonly CURRENT_TIMESTAMP = 58;
	public static readonly CURRENT_USER = 59;
	public static readonly DATA = 60;
	public static readonly DATABASE = 61;
	public static readonly DATABASES = 62;
	public static readonly DBPROPERTIES = 63;
	public static readonly DEFINED = 64;
	public static readonly DELETE = 65;
	public static readonly DELIMITED = 66;
	public static readonly DESC = 67;
	public static readonly DESCRIBE = 68;
	public static readonly DFS = 69;
	public static readonly DIRECTORIES = 70;
	public static readonly DIRECTORY = 71;
	public static readonly DISTINCT = 72;
	public static readonly DISTRIBUTE = 73;
	public static readonly DIV = 74;
	public static readonly DROP = 75;
	public static readonly ELSE = 76;
	public static readonly END = 77;
	public static readonly ESCAPE = 78;
	public static readonly ESCAPED = 79;
	public static readonly EXCEPT = 80;
	public static readonly EXCHANGE = 81;
	public static readonly EXISTS = 82;
	public static readonly EXPLAIN = 83;
	public static readonly EXPORT = 84;
	public static readonly EXTENDED = 85;
	public static readonly EXTERNAL = 86;
	public static readonly EXTRACT = 87;
	public static readonly FALSE = 88;
	public static readonly FETCH = 89;
	public static readonly FIELDS = 90;
	public static readonly FILTER = 91;
	public static readonly FILEFORMAT = 92;
	public static readonly FIRST = 93;
	public static readonly FOLLOWING = 94;
	public static readonly FOR = 95;
	public static readonly FOREIGN = 96;
	public static readonly FORMAT = 97;
	public static readonly FORMATTED = 98;
	public static readonly FROM = 99;
	public static readonly FULL = 100;
	public static readonly FUNCTION = 101;
	public static readonly FUNCTIONS = 102;
	public static readonly GLOBAL = 103;
	public static readonly GRANT = 104;
	public static readonly GROUP = 105;
	public static readonly GROUPING = 106;
	public static readonly HAVING = 107;
	public static readonly IF = 108;
	public static readonly IGNORE = 109;
	public static readonly IMPORT = 110;
	public static readonly IN = 111;
	public static readonly INDEX = 112;
	public static readonly INDEXES = 113;
	public static readonly INNER = 114;
	public static readonly INPATH = 115;
	public static readonly INPUTFORMAT = 116;
	public static readonly INSERT = 117;
	public static readonly INTERSECT = 118;
	public static readonly INTERVAL = 119;
	public static readonly INTO = 120;
	public static readonly IS = 121;
	public static readonly ITEMS = 122;
	public static readonly JOIN = 123;
	public static readonly KEYS = 124;
	public static readonly LAST = 125;
	public static readonly LATERAL = 126;
	public static readonly LAZY = 127;
	public static readonly LEADING = 128;
	public static readonly LEFT = 129;
	public static readonly LIKE = 130;
	public static readonly LIMIT = 131;
	public static readonly LINES = 132;
	public static readonly LIST = 133;
	public static readonly LOAD = 134;
	public static readonly LOCAL = 135;
	public static readonly LOCATION = 136;
	public static readonly LOCK = 137;
	public static readonly LOCKS = 138;
	public static readonly LOGICAL = 139;
	public static readonly MACRO = 140;
	public static readonly MAP = 141;
	public static readonly MATCHED = 142;
	public static readonly MERGE = 143;
	public static readonly MSCK = 144;
	public static readonly NAMESPACE = 145;
	public static readonly NAMESPACES = 146;
	public static readonly NATURAL = 147;
	public static readonly NO = 148;
	public static readonly NOT = 149;
	public static readonly NULL = 150;
	public static readonly NULLS = 151;
	public static readonly OF = 152;
	public static readonly ON = 153;
	public static readonly ONLY = 154;
	public static readonly OPTION = 155;
	public static readonly OPTIONS = 156;
	public static readonly OR = 157;
	public static readonly ORDER = 158;
	public static readonly OUT = 159;
	public static readonly OUTER = 160;
	public static readonly OUTPUTFORMAT = 161;
	public static readonly OVER = 162;
	public static readonly OVERLAPS = 163;
	public static readonly OVERLAY = 164;
	public static readonly OVERWRITE = 165;
	public static readonly PARTITION = 166;
	public static readonly PARTITIONED = 167;
	public static readonly PARTITIONS = 168;
	public static readonly PERCENTLIT = 169;
	public static readonly PIVOT = 170;
	public static readonly PLACING = 171;
	public static readonly POSITION = 172;
	public static readonly PRECEDING = 173;
	public static readonly PRIMARY = 174;
	public static readonly PRINCIPALS = 175;
	public static readonly PROPERTIES = 176;
	public static readonly PURGE = 177;
	public static readonly QUERY = 178;
	public static readonly RANGE = 179;
	public static readonly RECORDREADER = 180;
	public static readonly RECORDWRITER = 181;
	public static readonly RECOVER = 182;
	public static readonly REDUCE = 183;
	public static readonly REFERENCES = 184;
	public static readonly REFRESH = 185;
	public static readonly RENAME = 186;
	public static readonly REPAIR = 187;
	public static readonly REPLACE = 188;
	public static readonly RESET = 189;
	public static readonly RESTRICT = 190;
	public static readonly REVOKE = 191;
	public static readonly RIGHT = 192;
	public static readonly RLIKE = 193;
	public static readonly ROLE = 194;
	public static readonly ROLES = 195;
	public static readonly ROLLBACK = 196;
	public static readonly ROLLUP = 197;
	public static readonly ROW = 198;
	public static readonly ROWS = 199;
	public static readonly SCHEMA = 200;
	public static readonly SELECT = 201;
	public static readonly SEMI = 202;
	public static readonly SEPARATED = 203;
	public static readonly SERDE = 204;
	public static readonly SERDEPROPERTIES = 205;
	public static readonly SESSION_USER = 206;
	public static readonly SET = 207;
	public static readonly SETMINUS = 208;
	public static readonly SETS = 209;
	public static readonly SHOW = 210;
	public static readonly SKEWED = 211;
	public static readonly SOME = 212;
	public static readonly SORT = 213;
	public static readonly SORTED = 214;
	public static readonly START = 215;
	public static readonly STATISTICS = 216;
	public static readonly STORED = 217;
	public static readonly STRATIFY = 218;
	public static readonly STRUCT = 219;
	public static readonly SUBSTR = 220;
	public static readonly SUBSTRING = 221;
	public static readonly TABLE = 222;
	public static readonly TABLES = 223;
	public static readonly TABLESAMPLE = 224;
	public static readonly TBLPROPERTIES = 225;
	public static readonly TEMPORARY = 226;
	public static readonly TERMINATED = 227;
	public static readonly THEN = 228;
	public static readonly TIME = 229;
	public static readonly TO = 230;
	public static readonly TOUCH = 231;
	public static readonly TRAILING = 232;
	public static readonly TRANSACTION = 233;
	public static readonly TRANSACTIONS = 234;
	public static readonly TRANSFORM = 235;
	public static readonly TRIM = 236;
	public static readonly TRUE = 237;
	public static readonly TRUNCATE = 238;
	public static readonly TYPE = 239;
	public static readonly UNARCHIVE = 240;
	public static readonly UNBOUNDED = 241;
	public static readonly UNCACHE = 242;
	public static readonly UNION = 243;
	public static readonly UNIQUE = 244;
	public static readonly UNKNOWN = 245;
	public static readonly UNLOCK = 246;
	public static readonly UNSET = 247;
	public static readonly UPDATE = 248;
	public static readonly USE = 249;
	public static readonly USER = 250;
	public static readonly USING = 251;
	public static readonly VALUES = 252;
	public static readonly VIEW = 253;
	public static readonly VIEWS = 254;
	public static readonly WHEN = 255;
	public static readonly WHERE = 256;
	public static readonly WINDOW = 257;
	public static readonly WITH = 258;
	public static readonly ZONE = 259;
	public static readonly EQ = 260;
	public static readonly NSEQ = 261;
	public static readonly NEQ = 262;
	public static readonly NEQJ = 263;
	public static readonly LT = 264;
	public static readonly LTE = 265;
	public static readonly GT = 266;
	public static readonly GTE = 267;
	public static readonly PLUS = 268;
	public static readonly MINUS = 269;
	public static readonly ASTERISK = 270;
	public static readonly SLASH = 271;
	public static readonly PERCENT = 272;
	public static readonly TILDE = 273;
	public static readonly AMPERSAND = 274;
	public static readonly PIPE = 275;
	public static readonly CONCAT_PIPE = 276;
	public static readonly HAT = 277;
	public static readonly SEMICOLON = 278;
	public static readonly STRING = 279;
	public static readonly BIGINT_LITERAL = 280;
	public static readonly SMALLINT_LITERAL = 281;
	public static readonly TINYINT_LITERAL = 282;
	public static readonly INTEGER_VALUE = 283;
	public static readonly EXPONENT_VALUE = 284;
	public static readonly DECIMAL_VALUE = 285;
	public static readonly FLOAT_LITERAL = 286;
	public static readonly DOUBLE_LITERAL = 287;
	public static readonly BIGDECIMAL_LITERAL = 288;
	public static readonly IDENTIFIER = 289;
	public static readonly BACKQUOTED_IDENTIFIER = 290;
	public static readonly SIMPLE_COMMENT = 291;
	public static readonly BRACKETED_COMMENT = 292;
	public static readonly WS = 293;
	public static readonly UNRECOGNIZED = 294;
	public static readonly RULE_program = 0;
	public static readonly RULE_singleStatement = 1;
	public static readonly RULE_emptyStatement = 2;
	public static readonly RULE_singleExpression = 3;
	public static readonly RULE_singleTableIdentifier = 4;
	public static readonly RULE_singleMultipartIdentifier = 5;
	public static readonly RULE_singleDataType = 6;
	public static readonly RULE_singleTableSchema = 7;
	public static readonly RULE_statement = 8;
	public static readonly RULE_configKey = 9;
	public static readonly RULE_unsupportedHiveNativeCommands = 10;
	public static readonly RULE_createTableHeader = 11;
	public static readonly RULE_replaceTableHeader = 12;
	public static readonly RULE_bucketSpec = 13;
	public static readonly RULE_skewSpec = 14;
	public static readonly RULE_locationSpec = 15;
	public static readonly RULE_commentSpec = 16;
	public static readonly RULE_query = 17;
	public static readonly RULE_insertInto = 18;
	public static readonly RULE_partitionSpecLocation = 19;
	public static readonly RULE_partitionSpec = 20;
	public static readonly RULE_partitionVal = 21;
	public static readonly RULE_namespace = 22;
	public static readonly RULE_describeFuncName = 23;
	public static readonly RULE_describeColName = 24;
	public static readonly RULE_ctes = 25;
	public static readonly RULE_namedQuery = 26;
	public static readonly RULE_tableProvider = 27;
	public static readonly RULE_createTableClauses = 28;
	public static readonly RULE_tablePropertyList = 29;
	public static readonly RULE_tableProperty = 30;
	public static readonly RULE_tablePropertyKey = 31;
	public static readonly RULE_tablePropertyValue = 32;
	public static readonly RULE_constantList = 33;
	public static readonly RULE_nestedConstantList = 34;
	public static readonly RULE_createFileFormat = 35;
	public static readonly RULE_fileFormat = 36;
	public static readonly RULE_storageHandler = 37;
	public static readonly RULE_resource = 38;
	public static readonly RULE_dmlStatementNoWith = 39;
	public static readonly RULE_queryOrganization = 40;
	public static readonly RULE_multiInsertQueryBody = 41;
	public static readonly RULE_queryTerm = 42;
	public static readonly RULE_queryPrimary = 43;
	public static readonly RULE_sortItem = 44;
	public static readonly RULE_fromStatement = 45;
	public static readonly RULE_fromStatementBody = 46;
	public static readonly RULE_querySpecification = 47;
	public static readonly RULE_transformClause = 48;
	public static readonly RULE_selectClause = 49;
	public static readonly RULE_setClause = 50;
	public static readonly RULE_matchedClause = 51;
	public static readonly RULE_notMatchedClause = 52;
	public static readonly RULE_matchedAction = 53;
	public static readonly RULE_notMatchedAction = 54;
	public static readonly RULE_assignmentList = 55;
	public static readonly RULE_assignment = 56;
	public static readonly RULE_whereClause = 57;
	public static readonly RULE_havingClause = 58;
	public static readonly RULE_hint = 59;
	public static readonly RULE_hintStatement = 60;
	public static readonly RULE_fromClause = 61;
	public static readonly RULE_suggestionRelation = 62;
	public static readonly RULE_aggregationClause = 63;
	public static readonly RULE_groupingSet = 64;
	public static readonly RULE_pivotClause = 65;
	public static readonly RULE_pivotColumn = 66;
	public static readonly RULE_pivotValue = 67;
	public static readonly RULE_lateralView = 68;
	public static readonly RULE_setQuantifier = 69;
	public static readonly RULE_relation = 70;
	public static readonly RULE_joinRelation = 71;
	public static readonly RULE_joinType = 72;
	public static readonly RULE_joinCriteria = 73;
	public static readonly RULE_sample = 74;
	public static readonly RULE_sampleMethod = 75;
	public static readonly RULE_identifierList = 76;
	public static readonly RULE_identifierSeq = 77;
	public static readonly RULE_orderedIdentifierList = 78;
	public static readonly RULE_orderedIdentifier = 79;
	public static readonly RULE_identifierCommentList = 80;
	public static readonly RULE_identifierComment = 81;
	public static readonly RULE_relationPrimary = 82;
	public static readonly RULE_inlineTable = 83;
	public static readonly RULE_functionTable = 84;
	public static readonly RULE_tableAlias = 85;
	public static readonly RULE_rowFormat = 86;
	public static readonly RULE_multipartIdentifierList = 87;
	public static readonly RULE_multipartIdentifier = 88;
	public static readonly RULE_tableIdentifier = 89;
	public static readonly RULE_namedExpression = 90;
	public static readonly RULE_namedExpressionSeq = 91;
	public static readonly RULE_transformList = 92;
	public static readonly RULE_transform = 93;
	public static readonly RULE_transformArgument = 94;
	public static readonly RULE_expression = 95;
	public static readonly RULE_booleanExpression = 96;
	public static readonly RULE_predicate = 97;
	public static readonly RULE_valueExpression = 98;
	public static readonly RULE_primaryExpression = 99;
	public static readonly RULE_constant = 100;
	public static readonly RULE_comparisonOperator = 101;
	public static readonly RULE_arithmeticOperator = 102;
	public static readonly RULE_predicateOperator = 103;
	public static readonly RULE_booleanValue = 104;
	public static readonly RULE_interval = 105;
	public static readonly RULE_errorCapturingMultiUnitsInterval = 106;
	public static readonly RULE_multiUnitsInterval = 107;
	public static readonly RULE_errorCapturingUnitToUnitInterval = 108;
	public static readonly RULE_unitToUnitInterval = 109;
	public static readonly RULE_intervalValue = 110;
	public static readonly RULE_colPosition = 111;
	public static readonly RULE_dataType = 112;
	public static readonly RULE_qualifiedColTypeWithPositionList = 113;
	public static readonly RULE_qualifiedColTypeWithPosition = 114;
	public static readonly RULE_colTypeList = 115;
	public static readonly RULE_colType = 116;
	public static readonly RULE_complexColTypeList = 117;
	public static readonly RULE_complexColType = 118;
	public static readonly RULE_whenClause = 119;
	public static readonly RULE_windowClause = 120;
	public static readonly RULE_namedWindow = 121;
	public static readonly RULE_windowSpec = 122;
	public static readonly RULE_windowFrame = 123;
	public static readonly RULE_frameBound = 124;
	public static readonly RULE_qualifiedNameList = 125;
	public static readonly RULE_functionName = 126;
	public static readonly RULE_qualifiedName = 127;
	public static readonly RULE_errorCapturingIdentifier = 128;
	public static readonly RULE_errorCapturingIdentifierExtra = 129;
	public static readonly RULE_identifier = 130;
	public static readonly RULE_strictIdentifier = 131;
	public static readonly RULE_quotedIdentifier = 132;
	public static readonly RULE_number = 133;
	public static readonly RULE_alterColumnAction = 134;
	public static readonly RULE_ansiNonReserved = 135;
	public static readonly RULE_strictNonReserved = 136;
	public static readonly RULE_nonReserved = 137;
	// tslint:disable:no-trailing-whitespace
	public static readonly ruleNames: string[] = [
		"program", "singleStatement", "emptyStatement", "singleExpression", "singleTableIdentifier", 
		"singleMultipartIdentifier", "singleDataType", "singleTableSchema", "statement", 
		"configKey", "unsupportedHiveNativeCommands", "createTableHeader", "replaceTableHeader", 
		"bucketSpec", "skewSpec", "locationSpec", "commentSpec", "query", "insertInto", 
		"partitionSpecLocation", "partitionSpec", "partitionVal", "namespace", 
		"describeFuncName", "describeColName", "ctes", "namedQuery", "tableProvider", 
		"createTableClauses", "tablePropertyList", "tableProperty", "tablePropertyKey", 
		"tablePropertyValue", "constantList", "nestedConstantList", "createFileFormat", 
		"fileFormat", "storageHandler", "resource", "dmlStatementNoWith", "queryOrganization", 
		"multiInsertQueryBody", "queryTerm", "queryPrimary", "sortItem", "fromStatement", 
		"fromStatementBody", "querySpecification", "transformClause", "selectClause", 
		"setClause", "matchedClause", "notMatchedClause", "matchedAction", "notMatchedAction", 
		"assignmentList", "assignment", "whereClause", "havingClause", "hint", 
		"hintStatement", "fromClause", "suggestionRelation", "aggregationClause", 
		"groupingSet", "pivotClause", "pivotColumn", "pivotValue", "lateralView", 
		"setQuantifier", "relation", "joinRelation", "joinType", "joinCriteria", 
		"sample", "sampleMethod", "identifierList", "identifierSeq", "orderedIdentifierList", 
		"orderedIdentifier", "identifierCommentList", "identifierComment", "relationPrimary", 
		"inlineTable", "functionTable", "tableAlias", "rowFormat", "multipartIdentifierList", 
		"multipartIdentifier", "tableIdentifier", "namedExpression", "namedExpressionSeq", 
		"transformList", "transform", "transformArgument", "expression", "booleanExpression", 
		"predicate", "valueExpression", "primaryExpression", "constant", "comparisonOperator", 
		"arithmeticOperator", "predicateOperator", "booleanValue", "interval", 
		"errorCapturingMultiUnitsInterval", "multiUnitsInterval", "errorCapturingUnitToUnitInterval", 
		"unitToUnitInterval", "intervalValue", "colPosition", "dataType", "qualifiedColTypeWithPositionList", 
		"qualifiedColTypeWithPosition", "colTypeList", "colType", "complexColTypeList", 
		"complexColType", "whenClause", "windowClause", "namedWindow", "windowSpec", 
		"windowFrame", "frameBound", "qualifiedNameList", "functionName", "qualifiedName", 
		"errorCapturingIdentifier", "errorCapturingIdentifierExtra", "identifier", 
		"strictIdentifier", "quotedIdentifier", "number", "alterColumnAction", 
		"ansiNonReserved", "strictNonReserved", "nonReserved",
	];

	private static readonly _LITERAL_NAMES: Array<string | undefined> = [
		undefined, "'('", "')'", "','", "'.'", "'/*+'", "'*/'", "'->'", "'['", 
		"']'", "':'", "'ADD'", "'AFTER'", "'ALL'", "'ALTER'", "'ANALYZE'", "'AND'", 
		"'ANTI'", "'ANY'", "'ARCHIVE'", "'ARRAY'", "'AS'", "'ASC'", "'AT'", "'AUTHORIZATION'", 
		"'BETWEEN'", "'BOTH'", "'BUCKET'", "'BUCKETS'", "'BY'", "'CACHE'", "'CASCADE'", 
		"'CASE'", "'CAST'", "'CHANGE'", "'CHECK'", "'CLEAR'", "'CLUSTER'", "'CLUSTERED'", 
		"'CODEGEN'", "'COLLATE'", "'COLLECTION'", "'COLUMN'", "'COLUMNS'", "'COMMENT'", 
		"'COMMIT'", "'COMPACT'", "'COMPACTIONS'", "'COMPUTE'", "'CONCATENATE'", 
		"'CONSTRAINT'", "'COST'", "'CREATE'", "'CROSS'", "'CUBE'", "'CURRENT'", 
		"'CURRENT_DATE'", "'CURRENT_TIME'", "'CURRENT_TIMESTAMP'", "'CURRENT_USER'", 
		"'DATA'", "'DATABASE'", undefined, "'DBPROPERTIES'", "'DEFINED'", "'DELETE'", 
		"'DELIMITED'", "'DESC'", "'DESCRIBE'", "'DFS'", "'DIRECTORIES'", "'DIRECTORY'", 
		"'DISTINCT'", "'DISTRIBUTE'", "'DIV'", "'DROP'", "'ELSE'", "'END'", "'ESCAPE'", 
		"'ESCAPED'", "'EXCEPT'", "'EXCHANGE'", "'EXISTS'", "'EXPLAIN'", "'EXPORT'", 
		"'EXTENDED'", "'EXTERNAL'", "'EXTRACT'", "'FALSE'", "'FETCH'", "'FIELDS'", 
		"'FILTER'", "'FILEFORMAT'", "'FIRST'", "'FOLLOWING'", "'FOR'", "'FOREIGN'", 
		"'FORMAT'", "'FORMATTED'", "'FROM'", "'FULL'", "'FUNCTION'", "'FUNCTIONS'", 
		"'GLOBAL'", "'GRANT'", "'GROUP'", "'GROUPING'", "'HAVING'", "'IF'", "'IGNORE'", 
		"'IMPORT'", "'IN'", "'INDEX'", "'INDEXES'", "'INNER'", "'INPATH'", "'INPUTFORMAT'", 
		"'INSERT'", "'INTERSECT'", "'INTERVAL'", "'INTO'", "'IS'", "'ITEMS'", 
		"'JOIN'", "'KEYS'", "'LAST'", "'LATERAL'", "'LAZY'", "'LEADING'", "'LEFT'", 
		"'LIKE'", "'LIMIT'", "'LINES'", "'LIST'", "'LOAD'", "'LOCAL'", "'LOCATION'", 
		"'LOCK'", "'LOCKS'", "'LOGICAL'", "'MACRO'", "'MAP'", "'MATCHED'", "'MERGE'", 
		"'MSCK'", "'NAMESPACE'", "'NAMESPACES'", "'NATURAL'", "'NO'", undefined, 
		"'NULL'", "'NULLS'", "'OF'", "'ON'", "'ONLY'", "'OPTION'", "'OPTIONS'", 
		"'OR'", "'ORDER'", "'OUT'", "'OUTER'", "'OUTPUTFORMAT'", "'OVER'", "'OVERLAPS'", 
		"'OVERLAY'", "'OVERWRITE'", "'PARTITION'", "'PARTITIONED'", "'PARTITIONS'", 
		"'PERCENT'", "'PIVOT'", "'PLACING'", "'POSITION'", "'PRECEDING'", "'PRIMARY'", 
		"'PRINCIPALS'", "'PROPERTIES'", "'PURGE'", "'QUERY'", "'RANGE'", "'RECORDREADER'", 
		"'RECORDWRITER'", "'RECOVER'", "'REDUCE'", "'REFERENCES'", "'REFRESH'", 
		"'RENAME'", "'REPAIR'", "'REPLACE'", "'RESET'", "'RESTRICT'", "'REVOKE'", 
		"'RIGHT'", undefined, "'ROLE'", "'ROLES'", "'ROLLBACK'", "'ROLLUP'", "'ROW'", 
		"'ROWS'", "'SCHEMA'", "'SELECT'", "'SEMI'", "'SEPARATED'", "'SERDE'", 
		"'SERDEPROPERTIES'", "'SESSION_USER'", "'SET'", "'MINUS'", "'SETS'", "'SHOW'", 
		"'SKEWED'", "'SOME'", "'SORT'", "'SORTED'", "'START'", "'STATISTICS'", 
		"'STORED'", "'STRATIFY'", "'STRUCT'", "'SUBSTR'", "'SUBSTRING'", "'TABLE'", 
		"'TABLES'", "'TABLESAMPLE'", "'TBLPROPERTIES'", undefined, "'TERMINATED'", 
		"'THEN'", "'TIME'", "'TO'", "'TOUCH'", "'TRAILING'", "'TRANSACTION'", 
		"'TRANSACTIONS'", "'TRANSFORM'", "'TRIM'", "'TRUE'", "'TRUNCATE'", "'TYPE'", 
		"'UNARCHIVE'", "'UNBOUNDED'", "'UNCACHE'", "'UNION'", "'UNIQUE'", "'UNKNOWN'", 
		"'UNLOCK'", "'UNSET'", "'UPDATE'", "'USE'", "'USER'", "'USING'", "'VALUES'", 
		"'VIEW'", "'VIEWS'", "'WHEN'", "'WHERE'", "'WINDOW'", "'WITH'", "'ZONE'", 
		undefined, "'<=>'", "'<>'", "'!='", "'<'", undefined, "'>'", undefined, 
		"'+'", "'-'", "'*'", "'/'", "'%'", "'~'", "'&'", "'|'", "'||'", "'^'", 
		"';'",
	];
	private static readonly _SYMBOLIC_NAMES: Array<string | undefined> = [
		undefined, undefined, undefined, undefined, undefined, undefined, undefined, 
		undefined, undefined, undefined, undefined, "ADD", "AFTER", "ALL", "ALTER", 
		"ANALYZE", "AND", "ANTI", "ANY", "ARCHIVE", "ARRAY", "AS", "ASC", "AT", 
		"AUTHORIZATION", "BETWEEN", "BOTH", "BUCKET", "BUCKETS", "BY", "CACHE", 
		"CASCADE", "CASE", "CAST", "CHANGE", "CHECK", "CLEAR", "CLUSTER", "CLUSTERED", 
		"CODEGEN", "COLLATE", "COLLECTION", "COLUMN", "COLUMNS", "COMMENT", "COMMIT", 
		"COMPACT", "COMPACTIONS", "COMPUTE", "CONCATENATE", "CONSTRAINT", "COST", 
		"CREATE", "CROSS", "CUBE", "CURRENT", "CURRENT_DATE", "CURRENT_TIME", 
		"CURRENT_TIMESTAMP", "CURRENT_USER", "DATA", "DATABASE", "DATABASES", 
		"DBPROPERTIES", "DEFINED", "DELETE", "DELIMITED", "DESC", "DESCRIBE", 
		"DFS", "DIRECTORIES", "DIRECTORY", "DISTINCT", "DISTRIBUTE", "DIV", "DROP", 
		"ELSE", "END", "ESCAPE", "ESCAPED", "EXCEPT", "EXCHANGE", "EXISTS", "EXPLAIN", 
		"EXPORT", "EXTENDED", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FIELDS", 
		"FILTER", "FILEFORMAT", "FIRST", "FOLLOWING", "FOR", "FOREIGN", "FORMAT", 
		"FORMATTED", "FROM", "FULL", "FUNCTION", "FUNCTIONS", "GLOBAL", "GRANT", 
		"GROUP", "GROUPING", "HAVING", "IF", "IGNORE", "IMPORT", "IN", "INDEX", 
		"INDEXES", "INNER", "INPATH", "INPUTFORMAT", "INSERT", "INTERSECT", "INTERVAL", 
		"INTO", "IS", "ITEMS", "JOIN", "KEYS", "LAST", "LATERAL", "LAZY", "LEADING", 
		"LEFT", "LIKE", "LIMIT", "LINES", "LIST", "LOAD", "LOCAL", "LOCATION", 
		"LOCK", "LOCKS", "LOGICAL", "MACRO", "MAP", "MATCHED", "MERGE", "MSCK", 
		"NAMESPACE", "NAMESPACES", "NATURAL", "NO", "NOT", "NULL", "NULLS", "OF", 
		"ON", "ONLY", "OPTION", "OPTIONS", "OR", "ORDER", "OUT", "OUTER", "OUTPUTFORMAT", 
		"OVER", "OVERLAPS", "OVERLAY", "OVERWRITE", "PARTITION", "PARTITIONED", 
		"PARTITIONS", "PERCENTLIT", "PIVOT", "PLACING", "POSITION", "PRECEDING", 
		"PRIMARY", "PRINCIPALS", "PROPERTIES", "PURGE", "QUERY", "RANGE", "RECORDREADER", 
		"RECORDWRITER", "RECOVER", "REDUCE", "REFERENCES", "REFRESH", "RENAME", 
		"REPAIR", "REPLACE", "RESET", "RESTRICT", "REVOKE", "RIGHT", "RLIKE", 
		"ROLE", "ROLES", "ROLLBACK", "ROLLUP", "ROW", "ROWS", "SCHEMA", "SELECT", 
		"SEMI", "SEPARATED", "SERDE", "SERDEPROPERTIES", "SESSION_USER", "SET", 
		"SETMINUS", "SETS", "SHOW", "SKEWED", "SOME", "SORT", "SORTED", "START", 
		"STATISTICS", "STORED", "STRATIFY", "STRUCT", "SUBSTR", "SUBSTRING", "TABLE", 
		"TABLES", "TABLESAMPLE", "TBLPROPERTIES", "TEMPORARY", "TERMINATED", "THEN", 
		"TIME", "TO", "TOUCH", "TRAILING", "TRANSACTION", "TRANSACTIONS", "TRANSFORM", 
		"TRIM", "TRUE", "TRUNCATE", "TYPE", "UNARCHIVE", "UNBOUNDED", "UNCACHE", 
		"UNION", "UNIQUE", "UNKNOWN", "UNLOCK", "UNSET", "UPDATE", "USE", "USER", 
		"USING", "VALUES", "VIEW", "VIEWS", "WHEN", "WHERE", "WINDOW", "WITH", 
		"ZONE", "EQ", "NSEQ", "NEQ", "NEQJ", "LT", "LTE", "GT", "GTE", "PLUS", 
		"MINUS", "ASTERISK", "SLASH", "PERCENT", "TILDE", "AMPERSAND", "PIPE", 
		"CONCAT_PIPE", "HAT", "SEMICOLON", "STRING", "BIGINT_LITERAL", "SMALLINT_LITERAL", 
		"TINYINT_LITERAL", "INTEGER_VALUE", "EXPONENT_VALUE", "DECIMAL_VALUE", 
		"FLOAT_LITERAL", "DOUBLE_LITERAL", "BIGDECIMAL_LITERAL", "IDENTIFIER", 
		"BACKQUOTED_IDENTIFIER", "SIMPLE_COMMENT", "BRACKETED_COMMENT", "WS", 
		"UNRECOGNIZED",
	];
	public static readonly VOCABULARY: Vocabulary = new VocabularyImpl(SparkSqlParser._LITERAL_NAMES, SparkSqlParser._SYMBOLIC_NAMES, []);

	// @Override
	// @NotNull
	public get vocabulary(): Vocabulary {
		return SparkSqlParser.VOCABULARY;
	}
	// tslint:enable:no-trailing-whitespace

	// @Override
	public get grammarFileName(): string { return "SparkSql.g4"; }

	// @Override
	public get ruleNames(): string[] { return SparkSqlParser.ruleNames; }

	// @Override
	public get serializedATN(): string { return SparkSqlParser._serializedATN; }

	protected createFailedPredicateException(predicate?: string, message?: string): FailedPredicateException {
		return new FailedPredicateException(this, predicate, message);
	}


	    /**
	    * When false, INTERSECT is given the greater precedence over the other set
	    * operations (UNION, EXCEPT and MINUS) as per the SQL standard.
	    */
	    //  public boolean legacy_setops_precedence_enbled = false;
	    /**
	    * When false, a literal with an exponent would be converted into
	    * double type rather than decimal type.
	    */
	    //  public boolean legacy_exponent_literal_as_decimal_enabled = false;
	    global.legacy_exponent_literal_as_decimal_enabled = false;
	    /**
	    * When true, the behavior of keywords follows ANSI SQL standard.
	    */
	    //  public boolean SQL_standard_keyword_behavior = false;

	    global.legacy_setops_precedence_enbled = false;
	    global.legacy_exponent_literal_as_decimal_enabled = false;
	    global.SQL_standard_keyword_behavior = false;

	constructor(input: TokenStream) {
		super(input);
		this._interp = new ParserATNSimulator(SparkSqlParser._ATN, this);
	}
	// @RuleVersion(0)
	public program(): ProgramContext {
		let _localctx: ProgramContext = new ProgramContext(this._ctx, this.state);
		this.enterRule(_localctx, 0, SparkSqlParser.RULE_program);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 276;
			this.singleStatement();
			this.state = 277;
			this.match(SparkSqlParser.EOF);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public singleStatement(): SingleStatementContext {
		let _localctx: SingleStatementContext = new SingleStatementContext(this._ctx, this.state);
		this.enterRule(_localctx, 2, SparkSqlParser.RULE_singleStatement);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 286;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while ((((_la) & ~0x1F) === 0 && ((1 << _la) & ((1 << SparkSqlParser.T__0) | (1 << SparkSqlParser.ADD) | (1 << SparkSqlParser.ALTER) | (1 << SparkSqlParser.ANALYZE) | (1 << SparkSqlParser.CACHE))) !== 0) || ((((_la - 36)) & ~0x1F) === 0 && ((1 << (_la - 36)) & ((1 << (SparkSqlParser.CLEAR - 36)) | (1 << (SparkSqlParser.COMMENT - 36)) | (1 << (SparkSqlParser.COMMIT - 36)) | (1 << (SparkSqlParser.CREATE - 36)) | (1 << (SparkSqlParser.DELETE - 36)) | (1 << (SparkSqlParser.DESC - 36)))) !== 0) || ((((_la - 68)) & ~0x1F) === 0 && ((1 << (_la - 68)) & ((1 << (SparkSqlParser.DESCRIBE - 68)) | (1 << (SparkSqlParser.DFS - 68)) | (1 << (SparkSqlParser.DROP - 68)) | (1 << (SparkSqlParser.EXPLAIN - 68)) | (1 << (SparkSqlParser.EXPORT - 68)) | (1 << (SparkSqlParser.FROM - 68)))) !== 0) || ((((_la - 104)) & ~0x1F) === 0 && ((1 << (_la - 104)) & ((1 << (SparkSqlParser.GRANT - 104)) | (1 << (SparkSqlParser.IMPORT - 104)) | (1 << (SparkSqlParser.INSERT - 104)) | (1 << (SparkSqlParser.LIST - 104)) | (1 << (SparkSqlParser.LOAD - 104)))) !== 0) || ((((_la - 137)) & ~0x1F) === 0 && ((1 << (_la - 137)) & ((1 << (SparkSqlParser.LOCK - 137)) | (1 << (SparkSqlParser.MAP - 137)) | (1 << (SparkSqlParser.MERGE - 137)) | (1 << (SparkSqlParser.MSCK - 137)))) !== 0) || ((((_la - 183)) & ~0x1F) === 0 && ((1 << (_la - 183)) & ((1 << (SparkSqlParser.REDUCE - 183)) | (1 << (SparkSqlParser.REFRESH - 183)) | (1 << (SparkSqlParser.REPLACE - 183)) | (1 << (SparkSqlParser.RESET - 183)) | (1 << (SparkSqlParser.REVOKE - 183)) | (1 << (SparkSqlParser.ROLLBACK - 183)) | (1 << (SparkSqlParser.SELECT - 183)) | (1 << (SparkSqlParser.SET - 183)) | (1 << (SparkSqlParser.SHOW - 183)))) !== 0) || ((((_la - 215)) & ~0x1F) === 0 && ((1 << (_la - 215)) & ((1 << (SparkSqlParser.START - 215)) | (1 << (SparkSqlParser.TABLE - 215)) | (1 << (SparkSqlParser.TRUNCATE - 215)) | (1 << (SparkSqlParser.UNCACHE - 215)) | (1 << (SparkSqlParser.UNLOCK - 215)))) !== 0) || ((((_la - 248)) & ~0x1F) === 0 && ((1 << (_la - 248)) & ((1 << (SparkSqlParser.UPDATE - 248)) | (1 << (SparkSqlParser.USE - 248)) | (1 << (SparkSqlParser.VALUES - 248)) | (1 << (SparkSqlParser.WITH - 248)) | (1 << (SparkSqlParser.SEMICOLON - 248)))) !== 0)) {
				{
				this.state = 284;
				this._errHandler.sync(this);
				switch (this._input.LA(1)) {
				case SparkSqlParser.T__0:
				case SparkSqlParser.ADD:
				case SparkSqlParser.ALTER:
				case SparkSqlParser.ANALYZE:
				case SparkSqlParser.CACHE:
				case SparkSqlParser.CLEAR:
				case SparkSqlParser.COMMENT:
				case SparkSqlParser.COMMIT:
				case SparkSqlParser.CREATE:
				case SparkSqlParser.DELETE:
				case SparkSqlParser.DESC:
				case SparkSqlParser.DESCRIBE:
				case SparkSqlParser.DFS:
				case SparkSqlParser.DROP:
				case SparkSqlParser.EXPLAIN:
				case SparkSqlParser.EXPORT:
				case SparkSqlParser.FROM:
				case SparkSqlParser.GRANT:
				case SparkSqlParser.IMPORT:
				case SparkSqlParser.INSERT:
				case SparkSqlParser.LIST:
				case SparkSqlParser.LOAD:
				case SparkSqlParser.LOCK:
				case SparkSqlParser.MAP:
				case SparkSqlParser.MERGE:
				case SparkSqlParser.MSCK:
				case SparkSqlParser.REDUCE:
				case SparkSqlParser.REFRESH:
				case SparkSqlParser.REPLACE:
				case SparkSqlParser.RESET:
				case SparkSqlParser.REVOKE:
				case SparkSqlParser.ROLLBACK:
				case SparkSqlParser.SELECT:
				case SparkSqlParser.SET:
				case SparkSqlParser.SHOW:
				case SparkSqlParser.START:
				case SparkSqlParser.TABLE:
				case SparkSqlParser.TRUNCATE:
				case SparkSqlParser.UNCACHE:
				case SparkSqlParser.UNLOCK:
				case SparkSqlParser.UPDATE:
				case SparkSqlParser.USE:
				case SparkSqlParser.VALUES:
				case SparkSqlParser.WITH:
					{
					this.state = 279;
					this.statement();
					this.state = 281;
					this._errHandler.sync(this);
					switch ( this.interpreter.adaptivePredict(this._input, 0, this._ctx) ) {
					case 1:
						{
						this.state = 280;
						this.match(SparkSqlParser.SEMICOLON);
						}
						break;
					}
					}
					break;
				case SparkSqlParser.SEMICOLON:
					{
					this.state = 283;
					this.emptyStatement();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				this.state = 288;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public emptyStatement(): EmptyStatementContext {
		let _localctx: EmptyStatementContext = new EmptyStatementContext(this._ctx, this.state);
		this.enterRule(_localctx, 4, SparkSqlParser.RULE_emptyStatement);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 289;
			this.match(SparkSqlParser.SEMICOLON);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public singleExpression(): SingleExpressionContext {
		let _localctx: SingleExpressionContext = new SingleExpressionContext(this._ctx, this.state);
		this.enterRule(_localctx, 6, SparkSqlParser.RULE_singleExpression);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 291;
			this.namedExpression();
			this.state = 292;
			this.match(SparkSqlParser.EOF);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public singleTableIdentifier(): SingleTableIdentifierContext {
		let _localctx: SingleTableIdentifierContext = new SingleTableIdentifierContext(this._ctx, this.state);
		this.enterRule(_localctx, 8, SparkSqlParser.RULE_singleTableIdentifier);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 294;
			this.tableIdentifier();
			this.state = 295;
			this.match(SparkSqlParser.EOF);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public singleMultipartIdentifier(): SingleMultipartIdentifierContext {
		let _localctx: SingleMultipartIdentifierContext = new SingleMultipartIdentifierContext(this._ctx, this.state);
		this.enterRule(_localctx, 10, SparkSqlParser.RULE_singleMultipartIdentifier);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 297;
			this.multipartIdentifier();
			this.state = 298;
			this.match(SparkSqlParser.EOF);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public singleDataType(): SingleDataTypeContext {
		let _localctx: SingleDataTypeContext = new SingleDataTypeContext(this._ctx, this.state);
		this.enterRule(_localctx, 12, SparkSqlParser.RULE_singleDataType);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 300;
			this.dataType();
			this.state = 301;
			this.match(SparkSqlParser.EOF);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public singleTableSchema(): SingleTableSchemaContext {
		let _localctx: SingleTableSchemaContext = new SingleTableSchemaContext(this._ctx, this.state);
		this.enterRule(_localctx, 14, SparkSqlParser.RULE_singleTableSchema);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 303;
			this.colTypeList();
			this.state = 304;
			this.match(SparkSqlParser.EOF);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public statement(): StatementContext {
		let _localctx: StatementContext = new StatementContext(this._ctx, this.state);
		this.enterRule(_localctx, 16, SparkSqlParser.RULE_statement);
		let _la: number;
		try {
			let _alt: number;
			this.state = 1050;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 112, this._ctx) ) {
			case 1:
				_localctx = new StatementDefaultContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 306;
				this.query();
				}
				break;

			case 2:
				_localctx = new DmlStatementContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 308;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.WITH) {
					{
					this.state = 307;
					this.ctes();
					}
				}

				this.state = 310;
				this.dmlStatementNoWith();
				}
				break;

			case 3:
				_localctx = new UseContext(_localctx);
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 311;
				this.match(SparkSqlParser.USE);
				this.state = 313;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 4, this._ctx) ) {
				case 1:
					{
					this.state = 312;
					this.match(SparkSqlParser.NAMESPACE);
					}
					break;
				}
				this.state = 315;
				this.multipartIdentifier();
				}
				break;

			case 4:
				_localctx = new CreateNamespaceContext(_localctx);
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 316;
				this.match(SparkSqlParser.CREATE);
				this.state = 317;
				this.namespace();
				this.state = 321;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 5, this._ctx) ) {
				case 1:
					{
					this.state = 318;
					this.match(SparkSqlParser.IF);
					this.state = 319;
					this.match(SparkSqlParser.NOT);
					this.state = 320;
					this.match(SparkSqlParser.EXISTS);
					}
					break;
				}
				this.state = 323;
				this.multipartIdentifier();
				this.state = 331;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 7, this._ctx);
				while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1) {
						{
						this.state = 329;
						this._errHandler.sync(this);
						switch (this._input.LA(1)) {
						case SparkSqlParser.COMMENT:
							{
							this.state = 324;
							this.commentSpec();
							}
							break;
						case SparkSqlParser.LOCATION:
							{
							this.state = 325;
							this.locationSpec();
							}
							break;
						case SparkSqlParser.WITH:
							{
							{
							this.state = 326;
							this.match(SparkSqlParser.WITH);
							this.state = 327;
							_la = this._input.LA(1);
							if (!(_la === SparkSqlParser.DBPROPERTIES || _la === SparkSqlParser.PROPERTIES)) {
							this._errHandler.recoverInline(this);
							} else {
								if (this._input.LA(1) === Token.EOF) {
									this.matchedEOF = true;
								}

								this._errHandler.reportMatch(this);
								this.consume();
							}
							this.state = 328;
							this.tablePropertyList();
							}
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						}
					}
					this.state = 333;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 7, this._ctx);
				}
				}
				break;

			case 5:
				_localctx = new SetNamespacePropertiesContext(_localctx);
				this.enterOuterAlt(_localctx, 5);
				{
				this.state = 334;
				this.match(SparkSqlParser.ALTER);
				this.state = 335;
				this.namespace();
				this.state = 336;
				this.multipartIdentifier();
				this.state = 337;
				this.match(SparkSqlParser.SET);
				this.state = 338;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.DBPROPERTIES || _la === SparkSqlParser.PROPERTIES)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 339;
				this.tablePropertyList();
				}
				break;

			case 6:
				_localctx = new SetNamespaceLocationContext(_localctx);
				this.enterOuterAlt(_localctx, 6);
				{
				this.state = 341;
				this.match(SparkSqlParser.ALTER);
				this.state = 342;
				this.namespace();
				this.state = 343;
				this.multipartIdentifier();
				this.state = 344;
				this.match(SparkSqlParser.SET);
				this.state = 345;
				this.locationSpec();
				}
				break;

			case 7:
				_localctx = new DropNamespaceContext(_localctx);
				this.enterOuterAlt(_localctx, 7);
				{
				this.state = 347;
				this.match(SparkSqlParser.DROP);
				this.state = 348;
				this.namespace();
				this.state = 351;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 8, this._ctx) ) {
				case 1:
					{
					this.state = 349;
					this.match(SparkSqlParser.IF);
					this.state = 350;
					this.match(SparkSqlParser.EXISTS);
					}
					break;
				}
				this.state = 353;
				this.multipartIdentifier();
				this.state = 355;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.CASCADE || _la === SparkSqlParser.RESTRICT) {
					{
					this.state = 354;
					_la = this._input.LA(1);
					if (!(_la === SparkSqlParser.CASCADE || _la === SparkSqlParser.RESTRICT)) {
					this._errHandler.recoverInline(this);
					} else {
						if (this._input.LA(1) === Token.EOF) {
							this.matchedEOF = true;
						}

						this._errHandler.reportMatch(this);
						this.consume();
					}
					}
				}

				}
				break;

			case 8:
				_localctx = new ShowNamespacesContext(_localctx);
				this.enterOuterAlt(_localctx, 8);
				{
				this.state = 357;
				this.match(SparkSqlParser.SHOW);
				this.state = 358;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.DATABASES || _la === SparkSqlParser.NAMESPACES)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 361;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 10, this._ctx) ) {
				case 1:
					{
					this.state = 359;
					_la = this._input.LA(1);
					if (!(_la === SparkSqlParser.FROM || _la === SparkSqlParser.IN)) {
					this._errHandler.recoverInline(this);
					} else {
						if (this._input.LA(1) === Token.EOF) {
							this.matchedEOF = true;
						}

						this._errHandler.reportMatch(this);
						this.consume();
					}
					this.state = 360;
					this.multipartIdentifier();
					}
					break;
				}
				this.state = 367;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.LIKE || _la === SparkSqlParser.STRING) {
					{
					this.state = 364;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					if (_la === SparkSqlParser.LIKE) {
						{
						this.state = 363;
						this.match(SparkSqlParser.LIKE);
						}
					}

					this.state = 366;
					(_localctx as ShowNamespacesContext)._pattern = this.match(SparkSqlParser.STRING);
					}
				}

				}
				break;

			case 9:
				_localctx = new CreateTableContext(_localctx);
				this.enterOuterAlt(_localctx, 9);
				{
				this.state = 369;
				this.createTableHeader();
				this.state = 374;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.T__0) {
					{
					this.state = 370;
					this.match(SparkSqlParser.T__0);
					this.state = 371;
					this.colTypeList();
					this.state = 372;
					this.match(SparkSqlParser.T__1);
					}
				}

				this.state = 376;
				this.tableProvider();
				this.state = 377;
				this.createTableClauses();
				this.state = 382;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 15, this._ctx) ) {
				case 1:
					{
					this.state = 379;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					if (_la === SparkSqlParser.AS) {
						{
						this.state = 378;
						this.match(SparkSqlParser.AS);
						}
					}

					this.state = 381;
					this.query();
					}
					break;
				}
				}
				break;

			case 10:
				_localctx = new CreateHiveTableContext(_localctx);
				this.enterOuterAlt(_localctx, 10);
				{
				this.state = 384;
				this.createTableHeader();
				this.state = 389;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 16, this._ctx) ) {
				case 1:
					{
					this.state = 385;
					this.match(SparkSqlParser.T__0);
					this.state = 386;
					(_localctx as CreateHiveTableContext)._columns = this.colTypeList();
					this.state = 387;
					this.match(SparkSqlParser.T__1);
					}
					break;
				}
				this.state = 412;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 19, this._ctx);
				while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1) {
						{
						this.state = 410;
						this._errHandler.sync(this);
						switch (this._input.LA(1)) {
						case SparkSqlParser.COMMENT:
							{
							this.state = 391;
							this.commentSpec();
							}
							break;
						case SparkSqlParser.PARTITIONED:
							{
							this.state = 401;
							this._errHandler.sync(this);
							switch ( this.interpreter.adaptivePredict(this._input, 17, this._ctx) ) {
							case 1:
								{
								this.state = 392;
								this.match(SparkSqlParser.PARTITIONED);
								this.state = 393;
								this.match(SparkSqlParser.BY);
								this.state = 394;
								this.match(SparkSqlParser.T__0);
								this.state = 395;
								(_localctx as CreateHiveTableContext)._partitionColumns = this.colTypeList();
								this.state = 396;
								this.match(SparkSqlParser.T__1);
								}
								break;

							case 2:
								{
								this.state = 398;
								this.match(SparkSqlParser.PARTITIONED);
								this.state = 399;
								this.match(SparkSqlParser.BY);
								this.state = 400;
								(_localctx as CreateHiveTableContext)._partitionColumnNames = this.identifierList();
								}
								break;
							}
							}
							break;
						case SparkSqlParser.CLUSTERED:
							{
							this.state = 403;
							this.bucketSpec();
							}
							break;
						case SparkSqlParser.SKEWED:
							{
							this.state = 404;
							this.skewSpec();
							}
							break;
						case SparkSqlParser.ROW:
							{
							this.state = 405;
							this.rowFormat();
							}
							break;
						case SparkSqlParser.STORED:
							{
							this.state = 406;
							this.createFileFormat();
							}
							break;
						case SparkSqlParser.LOCATION:
							{
							this.state = 407;
							this.locationSpec();
							}
							break;
						case SparkSqlParser.TBLPROPERTIES:
							{
							{
							this.state = 408;
							this.match(SparkSqlParser.TBLPROPERTIES);
							this.state = 409;
							(_localctx as CreateHiveTableContext)._tableProps = this.tablePropertyList();
							}
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						}
					}
					this.state = 414;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 19, this._ctx);
				}
				this.state = 419;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 21, this._ctx) ) {
				case 1:
					{
					this.state = 416;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					if (_la === SparkSqlParser.AS) {
						{
						this.state = 415;
						this.match(SparkSqlParser.AS);
						}
					}

					this.state = 418;
					this.query();
					}
					break;
				}
				}
				break;

			case 11:
				_localctx = new CreateTableLikeContext(_localctx);
				this.enterOuterAlt(_localctx, 11);
				{
				this.state = 421;
				this.match(SparkSqlParser.CREATE);
				this.state = 422;
				this.match(SparkSqlParser.TABLE);
				this.state = 426;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 22, this._ctx) ) {
				case 1:
					{
					this.state = 423;
					this.match(SparkSqlParser.IF);
					this.state = 424;
					this.match(SparkSqlParser.NOT);
					this.state = 425;
					this.match(SparkSqlParser.EXISTS);
					}
					break;
				}
				this.state = 428;
				(_localctx as CreateTableLikeContext)._target = this.tableIdentifier();
				this.state = 429;
				this.match(SparkSqlParser.LIKE);
				this.state = 430;
				(_localctx as CreateTableLikeContext)._source = this.tableIdentifier();
				this.state = 439;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === SparkSqlParser.LOCATION || ((((_la - 198)) & ~0x1F) === 0 && ((1 << (_la - 198)) & ((1 << (SparkSqlParser.ROW - 198)) | (1 << (SparkSqlParser.STORED - 198)) | (1 << (SparkSqlParser.TBLPROPERTIES - 198)))) !== 0) || _la === SparkSqlParser.USING) {
					{
					this.state = 437;
					this._errHandler.sync(this);
					switch (this._input.LA(1)) {
					case SparkSqlParser.USING:
						{
						this.state = 431;
						this.tableProvider();
						}
						break;
					case SparkSqlParser.ROW:
						{
						this.state = 432;
						this.rowFormat();
						}
						break;
					case SparkSqlParser.STORED:
						{
						this.state = 433;
						this.createFileFormat();
						}
						break;
					case SparkSqlParser.LOCATION:
						{
						this.state = 434;
						this.locationSpec();
						}
						break;
					case SparkSqlParser.TBLPROPERTIES:
						{
						{
						this.state = 435;
						this.match(SparkSqlParser.TBLPROPERTIES);
						this.state = 436;
						(_localctx as CreateTableLikeContext)._tableProps = this.tablePropertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					this.state = 441;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				}
				break;

			case 12:
				_localctx = new ReplaceTableContext(_localctx);
				this.enterOuterAlt(_localctx, 12);
				{
				this.state = 442;
				this.replaceTableHeader();
				this.state = 447;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.T__0) {
					{
					this.state = 443;
					this.match(SparkSqlParser.T__0);
					this.state = 444;
					this.colTypeList();
					this.state = 445;
					this.match(SparkSqlParser.T__1);
					}
				}

				this.state = 449;
				this.tableProvider();
				this.state = 450;
				this.createTableClauses();
				this.state = 455;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 27, this._ctx) ) {
				case 1:
					{
					this.state = 452;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					if (_la === SparkSqlParser.AS) {
						{
						this.state = 451;
						this.match(SparkSqlParser.AS);
						}
					}

					this.state = 454;
					this.query();
					}
					break;
				}
				}
				break;

			case 13:
				_localctx = new AnalyzeContext(_localctx);
				this.enterOuterAlt(_localctx, 13);
				{
				this.state = 457;
				this.match(SparkSqlParser.ANALYZE);
				this.state = 458;
				this.match(SparkSqlParser.TABLE);
				this.state = 459;
				this.multipartIdentifier();
				this.state = 461;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 460;
					this.partitionSpec();
					}
				}

				this.state = 463;
				this.match(SparkSqlParser.COMPUTE);
				this.state = 464;
				this.match(SparkSqlParser.STATISTICS);
				this.state = 472;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 29, this._ctx) ) {
				case 1:
					{
					this.state = 465;
					this.identifier();
					}
					break;

				case 2:
					{
					this.state = 466;
					this.match(SparkSqlParser.FOR);
					this.state = 467;
					this.match(SparkSqlParser.COLUMNS);
					this.state = 468;
					this.identifierSeq();
					}
					break;

				case 3:
					{
					this.state = 469;
					this.match(SparkSqlParser.FOR);
					this.state = 470;
					this.match(SparkSqlParser.ALL);
					this.state = 471;
					this.match(SparkSqlParser.COLUMNS);
					}
					break;
				}
				}
				break;

			case 14:
				_localctx = new AddTableColumnsContext(_localctx);
				this.enterOuterAlt(_localctx, 14);
				{
				this.state = 474;
				this.match(SparkSqlParser.ALTER);
				this.state = 475;
				this.match(SparkSqlParser.TABLE);
				this.state = 476;
				this.multipartIdentifier();
				this.state = 477;
				this.match(SparkSqlParser.ADD);
				this.state = 478;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.COLUMN || _la === SparkSqlParser.COLUMNS)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 479;
				(_localctx as AddTableColumnsContext)._columns = this.qualifiedColTypeWithPositionList();
				}
				break;

			case 15:
				_localctx = new AddTableColumnsContext(_localctx);
				this.enterOuterAlt(_localctx, 15);
				{
				this.state = 481;
				this.match(SparkSqlParser.ALTER);
				this.state = 482;
				this.match(SparkSqlParser.TABLE);
				this.state = 483;
				this.multipartIdentifier();
				this.state = 484;
				this.match(SparkSqlParser.ADD);
				this.state = 485;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.COLUMN || _la === SparkSqlParser.COLUMNS)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 486;
				this.match(SparkSqlParser.T__0);
				this.state = 487;
				(_localctx as AddTableColumnsContext)._columns = this.qualifiedColTypeWithPositionList();
				this.state = 488;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 16:
				_localctx = new RenameTableColumnContext(_localctx);
				this.enterOuterAlt(_localctx, 16);
				{
				this.state = 490;
				this.match(SparkSqlParser.ALTER);
				this.state = 491;
				this.match(SparkSqlParser.TABLE);
				this.state = 492;
				(_localctx as RenameTableColumnContext)._table = this.multipartIdentifier();
				this.state = 493;
				this.match(SparkSqlParser.RENAME);
				this.state = 494;
				this.match(SparkSqlParser.COLUMN);
				this.state = 495;
				(_localctx as RenameTableColumnContext)._from = this.multipartIdentifier();
				this.state = 496;
				this.match(SparkSqlParser.TO);
				this.state = 497;
				(_localctx as RenameTableColumnContext)._to = this.errorCapturingIdentifier();
				}
				break;

			case 17:
				_localctx = new DropTableColumnsContext(_localctx);
				this.enterOuterAlt(_localctx, 17);
				{
				this.state = 499;
				this.match(SparkSqlParser.ALTER);
				this.state = 500;
				this.match(SparkSqlParser.TABLE);
				this.state = 501;
				this.multipartIdentifier();
				this.state = 502;
				this.match(SparkSqlParser.DROP);
				this.state = 503;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.COLUMN || _la === SparkSqlParser.COLUMNS)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 504;
				this.match(SparkSqlParser.T__0);
				this.state = 505;
				(_localctx as DropTableColumnsContext)._columns = this.multipartIdentifierList();
				this.state = 506;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 18:
				_localctx = new DropTableColumnsContext(_localctx);
				this.enterOuterAlt(_localctx, 18);
				{
				this.state = 508;
				this.match(SparkSqlParser.ALTER);
				this.state = 509;
				this.match(SparkSqlParser.TABLE);
				this.state = 510;
				this.multipartIdentifier();
				this.state = 511;
				this.match(SparkSqlParser.DROP);
				this.state = 512;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.COLUMN || _la === SparkSqlParser.COLUMNS)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 513;
				(_localctx as DropTableColumnsContext)._columns = this.multipartIdentifierList();
				}
				break;

			case 19:
				_localctx = new RenameTableContext(_localctx);
				this.enterOuterAlt(_localctx, 19);
				{
				this.state = 515;
				this.match(SparkSqlParser.ALTER);
				this.state = 516;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.TABLE || _la === SparkSqlParser.VIEW)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 517;
				(_localctx as RenameTableContext)._from = this.multipartIdentifier();
				this.state = 518;
				this.match(SparkSqlParser.RENAME);
				this.state = 519;
				this.match(SparkSqlParser.TO);
				this.state = 520;
				(_localctx as RenameTableContext)._to = this.multipartIdentifier();
				}
				break;

			case 20:
				_localctx = new SetTablePropertiesContext(_localctx);
				this.enterOuterAlt(_localctx, 20);
				{
				this.state = 522;
				this.match(SparkSqlParser.ALTER);
				this.state = 523;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.TABLE || _la === SparkSqlParser.VIEW)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 524;
				this.multipartIdentifier();
				this.state = 525;
				this.match(SparkSqlParser.SET);
				this.state = 526;
				this.match(SparkSqlParser.TBLPROPERTIES);
				this.state = 527;
				this.tablePropertyList();
				}
				break;

			case 21:
				_localctx = new UnsetTablePropertiesContext(_localctx);
				this.enterOuterAlt(_localctx, 21);
				{
				this.state = 529;
				this.match(SparkSqlParser.ALTER);
				this.state = 530;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.TABLE || _la === SparkSqlParser.VIEW)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 531;
				this.multipartIdentifier();
				this.state = 532;
				this.match(SparkSqlParser.UNSET);
				this.state = 533;
				this.match(SparkSqlParser.TBLPROPERTIES);
				this.state = 536;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.IF) {
					{
					this.state = 534;
					this.match(SparkSqlParser.IF);
					this.state = 535;
					this.match(SparkSqlParser.EXISTS);
					}
				}

				this.state = 538;
				this.tablePropertyList();
				}
				break;

			case 22:
				_localctx = new AlterTableAlterColumnContext(_localctx);
				this.enterOuterAlt(_localctx, 22);
				{
				this.state = 540;
				this.match(SparkSqlParser.ALTER);
				this.state = 541;
				this.match(SparkSqlParser.TABLE);
				this.state = 542;
				(_localctx as AlterTableAlterColumnContext)._table = this.multipartIdentifier();
				this.state = 543;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.ALTER || _la === SparkSqlParser.CHANGE)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 545;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 31, this._ctx) ) {
				case 1:
					{
					this.state = 544;
					this.match(SparkSqlParser.COLUMN);
					}
					break;
				}
				this.state = 547;
				(_localctx as AlterTableAlterColumnContext)._column = this.multipartIdentifier();
				this.state = 549;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 32, this._ctx) ) {
				case 1:
					{
					this.state = 548;
					this.alterColumnAction();
					}
					break;
				}
				}
				break;

			case 23:
				_localctx = new HiveChangeColumnContext(_localctx);
				this.enterOuterAlt(_localctx, 23);
				{
				this.state = 551;
				this.match(SparkSqlParser.ALTER);
				this.state = 552;
				this.match(SparkSqlParser.TABLE);
				this.state = 553;
				(_localctx as HiveChangeColumnContext)._table = this.multipartIdentifier();
				this.state = 555;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 554;
					this.partitionSpec();
					}
				}

				this.state = 557;
				this.match(SparkSqlParser.CHANGE);
				this.state = 559;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 34, this._ctx) ) {
				case 1:
					{
					this.state = 558;
					this.match(SparkSqlParser.COLUMN);
					}
					break;
				}
				this.state = 561;
				(_localctx as HiveChangeColumnContext)._colName = this.multipartIdentifier();
				this.state = 562;
				this.colType();
				this.state = 564;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.AFTER || _la === SparkSqlParser.FIRST) {
					{
					this.state = 563;
					this.colPosition();
					}
				}

				}
				break;

			case 24:
				_localctx = new HiveReplaceColumnsContext(_localctx);
				this.enterOuterAlt(_localctx, 24);
				{
				this.state = 566;
				this.match(SparkSqlParser.ALTER);
				this.state = 567;
				this.match(SparkSqlParser.TABLE);
				this.state = 568;
				(_localctx as HiveReplaceColumnsContext)._table = this.multipartIdentifier();
				this.state = 570;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 569;
					this.partitionSpec();
					}
				}

				this.state = 572;
				this.match(SparkSqlParser.REPLACE);
				this.state = 573;
				this.match(SparkSqlParser.COLUMNS);
				this.state = 574;
				this.match(SparkSqlParser.T__0);
				this.state = 575;
				(_localctx as HiveReplaceColumnsContext)._columns = this.qualifiedColTypeWithPositionList();
				this.state = 576;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 25:
				_localctx = new SetTableSerDeContext(_localctx);
				this.enterOuterAlt(_localctx, 25);
				{
				this.state = 578;
				this.match(SparkSqlParser.ALTER);
				this.state = 579;
				this.match(SparkSqlParser.TABLE);
				this.state = 580;
				this.multipartIdentifier();
				this.state = 582;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 581;
					this.partitionSpec();
					}
				}

				this.state = 584;
				this.match(SparkSqlParser.SET);
				this.state = 585;
				this.match(SparkSqlParser.SERDE);
				this.state = 586;
				this.match(SparkSqlParser.STRING);
				this.state = 590;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 38, this._ctx) ) {
				case 1:
					{
					this.state = 587;
					this.match(SparkSqlParser.WITH);
					this.state = 588;
					this.match(SparkSqlParser.SERDEPROPERTIES);
					this.state = 589;
					this.tablePropertyList();
					}
					break;
				}
				}
				break;

			case 26:
				_localctx = new SetTableSerDeContext(_localctx);
				this.enterOuterAlt(_localctx, 26);
				{
				this.state = 592;
				this.match(SparkSqlParser.ALTER);
				this.state = 593;
				this.match(SparkSqlParser.TABLE);
				this.state = 594;
				this.multipartIdentifier();
				this.state = 596;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 595;
					this.partitionSpec();
					}
				}

				this.state = 598;
				this.match(SparkSqlParser.SET);
				this.state = 599;
				this.match(SparkSqlParser.SERDEPROPERTIES);
				this.state = 600;
				this.tablePropertyList();
				}
				break;

			case 27:
				_localctx = new AddTablePartitionContext(_localctx);
				this.enterOuterAlt(_localctx, 27);
				{
				this.state = 602;
				this.match(SparkSqlParser.ALTER);
				this.state = 603;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.TABLE || _la === SparkSqlParser.VIEW)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 604;
				this.multipartIdentifier();
				this.state = 605;
				this.match(SparkSqlParser.ADD);
				this.state = 609;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.IF) {
					{
					this.state = 606;
					this.match(SparkSqlParser.IF);
					this.state = 607;
					this.match(SparkSqlParser.NOT);
					this.state = 608;
					this.match(SparkSqlParser.EXISTS);
					}
				}

				this.state = 612;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				do {
					{
					{
					this.state = 611;
					this.partitionSpecLocation();
					}
					}
					this.state = 614;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				} while (_la === SparkSqlParser.PARTITION);
				}
				break;

			case 28:
				_localctx = new RenameTablePartitionContext(_localctx);
				this.enterOuterAlt(_localctx, 28);
				{
				this.state = 616;
				this.match(SparkSqlParser.ALTER);
				this.state = 617;
				this.match(SparkSqlParser.TABLE);
				this.state = 618;
				this.multipartIdentifier();
				this.state = 619;
				(_localctx as RenameTablePartitionContext)._from = this.partitionSpec();
				this.state = 620;
				this.match(SparkSqlParser.RENAME);
				this.state = 621;
				this.match(SparkSqlParser.TO);
				this.state = 622;
				(_localctx as RenameTablePartitionContext)._to = this.partitionSpec();
				}
				break;

			case 29:
				_localctx = new DropTablePartitionsContext(_localctx);
				this.enterOuterAlt(_localctx, 29);
				{
				this.state = 624;
				this.match(SparkSqlParser.ALTER);
				this.state = 625;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.TABLE || _la === SparkSqlParser.VIEW)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 626;
				this.multipartIdentifier();
				this.state = 627;
				this.match(SparkSqlParser.DROP);
				this.state = 630;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.IF) {
					{
					this.state = 628;
					this.match(SparkSqlParser.IF);
					this.state = 629;
					this.match(SparkSqlParser.EXISTS);
					}
				}

				this.state = 632;
				this.partitionSpec();
				this.state = 637;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === SparkSqlParser.T__2) {
					{
					{
					this.state = 633;
					this.match(SparkSqlParser.T__2);
					this.state = 634;
					this.partitionSpec();
					}
					}
					this.state = 639;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				this.state = 641;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PURGE) {
					{
					this.state = 640;
					this.match(SparkSqlParser.PURGE);
					}
				}

				}
				break;

			case 30:
				_localctx = new SetTableLocationContext(_localctx);
				this.enterOuterAlt(_localctx, 30);
				{
				this.state = 643;
				this.match(SparkSqlParser.ALTER);
				this.state = 644;
				this.match(SparkSqlParser.TABLE);
				this.state = 645;
				this.multipartIdentifier();
				this.state = 647;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 646;
					this.partitionSpec();
					}
				}

				this.state = 649;
				this.match(SparkSqlParser.SET);
				this.state = 650;
				this.locationSpec();
				}
				break;

			case 31:
				_localctx = new RecoverPartitionsContext(_localctx);
				this.enterOuterAlt(_localctx, 31);
				{
				this.state = 652;
				this.match(SparkSqlParser.ALTER);
				this.state = 653;
				this.match(SparkSqlParser.TABLE);
				this.state = 654;
				this.multipartIdentifier();
				this.state = 655;
				this.match(SparkSqlParser.RECOVER);
				this.state = 656;
				this.match(SparkSqlParser.PARTITIONS);
				}
				break;

			case 32:
				_localctx = new DropTableContext(_localctx);
				this.enterOuterAlt(_localctx, 32);
				{
				this.state = 658;
				this.match(SparkSqlParser.DROP);
				this.state = 659;
				this.match(SparkSqlParser.TABLE);
				this.state = 662;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 46, this._ctx) ) {
				case 1:
					{
					this.state = 660;
					this.match(SparkSqlParser.IF);
					this.state = 661;
					this.match(SparkSqlParser.EXISTS);
					}
					break;
				}
				this.state = 664;
				this.multipartIdentifier();
				this.state = 666;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PURGE) {
					{
					this.state = 665;
					this.match(SparkSqlParser.PURGE);
					}
				}

				}
				break;

			case 33:
				_localctx = new DropViewContext(_localctx);
				this.enterOuterAlt(_localctx, 33);
				{
				this.state = 668;
				this.match(SparkSqlParser.DROP);
				this.state = 669;
				this.match(SparkSqlParser.VIEW);
				this.state = 672;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 48, this._ctx) ) {
				case 1:
					{
					this.state = 670;
					this.match(SparkSqlParser.IF);
					this.state = 671;
					this.match(SparkSqlParser.EXISTS);
					}
					break;
				}
				this.state = 674;
				this.multipartIdentifier();
				}
				break;

			case 34:
				_localctx = new CreateViewContext(_localctx);
				this.enterOuterAlt(_localctx, 34);
				{
				this.state = 675;
				this.match(SparkSqlParser.CREATE);
				this.state = 678;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.OR) {
					{
					this.state = 676;
					this.match(SparkSqlParser.OR);
					this.state = 677;
					this.match(SparkSqlParser.REPLACE);
					}
				}

				this.state = 684;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.GLOBAL || _la === SparkSqlParser.TEMPORARY) {
					{
					this.state = 681;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					if (_la === SparkSqlParser.GLOBAL) {
						{
						this.state = 680;
						this.match(SparkSqlParser.GLOBAL);
						}
					}

					this.state = 683;
					this.match(SparkSqlParser.TEMPORARY);
					}
				}

				this.state = 686;
				this.match(SparkSqlParser.VIEW);
				this.state = 690;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 52, this._ctx) ) {
				case 1:
					{
					this.state = 687;
					this.match(SparkSqlParser.IF);
					this.state = 688;
					this.match(SparkSqlParser.NOT);
					this.state = 689;
					this.match(SparkSqlParser.EXISTS);
					}
					break;
				}
				this.state = 692;
				this.multipartIdentifier();
				this.state = 694;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.T__0) {
					{
					this.state = 693;
					this.identifierCommentList();
					}
				}

				this.state = 704;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === SparkSqlParser.COMMENT || _la === SparkSqlParser.PARTITIONED || _la === SparkSqlParser.TBLPROPERTIES) {
					{
					this.state = 702;
					this._errHandler.sync(this);
					switch (this._input.LA(1)) {
					case SparkSqlParser.COMMENT:
						{
						this.state = 696;
						this.commentSpec();
						}
						break;
					case SparkSqlParser.PARTITIONED:
						{
						{
						this.state = 697;
						this.match(SparkSqlParser.PARTITIONED);
						this.state = 698;
						this.match(SparkSqlParser.ON);
						this.state = 699;
						this.identifierList();
						}
						}
						break;
					case SparkSqlParser.TBLPROPERTIES:
						{
						{
						this.state = 700;
						this.match(SparkSqlParser.TBLPROPERTIES);
						this.state = 701;
						this.tablePropertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					this.state = 706;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				this.state = 707;
				this.match(SparkSqlParser.AS);
				this.state = 708;
				this.query();
				}
				break;

			case 35:
				_localctx = new CreateTempViewUsingContext(_localctx);
				this.enterOuterAlt(_localctx, 35);
				{
				this.state = 710;
				this.match(SparkSqlParser.CREATE);
				this.state = 713;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.OR) {
					{
					this.state = 711;
					this.match(SparkSqlParser.OR);
					this.state = 712;
					this.match(SparkSqlParser.REPLACE);
					}
				}

				this.state = 716;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.GLOBAL) {
					{
					this.state = 715;
					this.match(SparkSqlParser.GLOBAL);
					}
				}

				this.state = 718;
				this.match(SparkSqlParser.TEMPORARY);
				this.state = 719;
				this.match(SparkSqlParser.VIEW);
				this.state = 720;
				this.tableIdentifier();
				this.state = 725;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.T__0) {
					{
					this.state = 721;
					this.match(SparkSqlParser.T__0);
					this.state = 722;
					this.colTypeList();
					this.state = 723;
					this.match(SparkSqlParser.T__1);
					}
				}

				this.state = 727;
				this.tableProvider();
				this.state = 730;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.OPTIONS) {
					{
					this.state = 728;
					this.match(SparkSqlParser.OPTIONS);
					this.state = 729;
					this.tablePropertyList();
					}
				}

				}
				break;

			case 36:
				_localctx = new AlterViewQueryContext(_localctx);
				this.enterOuterAlt(_localctx, 36);
				{
				this.state = 732;
				this.match(SparkSqlParser.ALTER);
				this.state = 733;
				this.match(SparkSqlParser.VIEW);
				this.state = 734;
				this.multipartIdentifier();
				this.state = 736;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.AS) {
					{
					this.state = 735;
					this.match(SparkSqlParser.AS);
					}
				}

				this.state = 738;
				this.query();
				}
				break;

			case 37:
				_localctx = new CreateFunctionContext(_localctx);
				this.enterOuterAlt(_localctx, 37);
				{
				this.state = 740;
				this.match(SparkSqlParser.CREATE);
				this.state = 743;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.OR) {
					{
					this.state = 741;
					this.match(SparkSqlParser.OR);
					this.state = 742;
					this.match(SparkSqlParser.REPLACE);
					}
				}

				this.state = 746;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.TEMPORARY) {
					{
					this.state = 745;
					this.match(SparkSqlParser.TEMPORARY);
					}
				}

				this.state = 748;
				this.match(SparkSqlParser.FUNCTION);
				this.state = 752;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 63, this._ctx) ) {
				case 1:
					{
					this.state = 749;
					this.match(SparkSqlParser.IF);
					this.state = 750;
					this.match(SparkSqlParser.NOT);
					this.state = 751;
					this.match(SparkSqlParser.EXISTS);
					}
					break;
				}
				this.state = 754;
				this.multipartIdentifier();
				this.state = 755;
				this.match(SparkSqlParser.AS);
				this.state = 756;
				(_localctx as CreateFunctionContext)._className = this.match(SparkSqlParser.STRING);
				this.state = 766;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.USING) {
					{
					this.state = 757;
					this.match(SparkSqlParser.USING);
					this.state = 758;
					this.resource();
					this.state = 763;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					while (_la === SparkSqlParser.T__2) {
						{
						{
						this.state = 759;
						this.match(SparkSqlParser.T__2);
						this.state = 760;
						this.resource();
						}
						}
						this.state = 765;
						this._errHandler.sync(this);
						_la = this._input.LA(1);
					}
					}
				}

				}
				break;

			case 38:
				_localctx = new DropFunctionContext(_localctx);
				this.enterOuterAlt(_localctx, 38);
				{
				this.state = 768;
				this.match(SparkSqlParser.DROP);
				this.state = 770;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.TEMPORARY) {
					{
					this.state = 769;
					this.match(SparkSqlParser.TEMPORARY);
					}
				}

				this.state = 772;
				this.match(SparkSqlParser.FUNCTION);
				this.state = 775;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 67, this._ctx) ) {
				case 1:
					{
					this.state = 773;
					this.match(SparkSqlParser.IF);
					this.state = 774;
					this.match(SparkSqlParser.EXISTS);
					}
					break;
				}
				this.state = 777;
				this.multipartIdentifier();
				}
				break;

			case 39:
				_localctx = new ExplainContext(_localctx);
				this.enterOuterAlt(_localctx, 39);
				{
				this.state = 778;
				this.match(SparkSqlParser.EXPLAIN);
				this.state = 780;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.CODEGEN || _la === SparkSqlParser.COST || _la === SparkSqlParser.EXTENDED || _la === SparkSqlParser.FORMATTED || _la === SparkSqlParser.LOGICAL) {
					{
					this.state = 779;
					_la = this._input.LA(1);
					if (!(_la === SparkSqlParser.CODEGEN || _la === SparkSqlParser.COST || _la === SparkSqlParser.EXTENDED || _la === SparkSqlParser.FORMATTED || _la === SparkSqlParser.LOGICAL)) {
					this._errHandler.recoverInline(this);
					} else {
						if (this._input.LA(1) === Token.EOF) {
							this.matchedEOF = true;
						}

						this._errHandler.reportMatch(this);
						this.consume();
					}
					}
				}

				this.state = 782;
				this.statement();
				}
				break;

			case 40:
				_localctx = new ShowTablesContext(_localctx);
				this.enterOuterAlt(_localctx, 40);
				{
				this.state = 783;
				this.match(SparkSqlParser.SHOW);
				this.state = 784;
				this.match(SparkSqlParser.TABLES);
				this.state = 787;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 69, this._ctx) ) {
				case 1:
					{
					this.state = 785;
					_la = this._input.LA(1);
					if (!(_la === SparkSqlParser.FROM || _la === SparkSqlParser.IN)) {
					this._errHandler.recoverInline(this);
					} else {
						if (this._input.LA(1) === Token.EOF) {
							this.matchedEOF = true;
						}

						this._errHandler.reportMatch(this);
						this.consume();
					}
					this.state = 786;
					this.multipartIdentifier();
					}
					break;
				}
				this.state = 793;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.LIKE || _la === SparkSqlParser.STRING) {
					{
					this.state = 790;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					if (_la === SparkSqlParser.LIKE) {
						{
						this.state = 789;
						this.match(SparkSqlParser.LIKE);
						}
					}

					this.state = 792;
					(_localctx as ShowTablesContext)._pattern = this.match(SparkSqlParser.STRING);
					}
				}

				}
				break;

			case 41:
				_localctx = new ShowTableContext(_localctx);
				this.enterOuterAlt(_localctx, 41);
				{
				this.state = 795;
				this.match(SparkSqlParser.SHOW);
				this.state = 796;
				this.match(SparkSqlParser.TABLE);
				this.state = 797;
				this.match(SparkSqlParser.EXTENDED);
				this.state = 800;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.FROM || _la === SparkSqlParser.IN) {
					{
					this.state = 798;
					_la = this._input.LA(1);
					if (!(_la === SparkSqlParser.FROM || _la === SparkSqlParser.IN)) {
					this._errHandler.recoverInline(this);
					} else {
						if (this._input.LA(1) === Token.EOF) {
							this.matchedEOF = true;
						}

						this._errHandler.reportMatch(this);
						this.consume();
					}
					this.state = 799;
					(_localctx as ShowTableContext)._ns = this.multipartIdentifier();
					}
				}

				this.state = 802;
				this.match(SparkSqlParser.LIKE);
				this.state = 803;
				(_localctx as ShowTableContext)._pattern = this.match(SparkSqlParser.STRING);
				this.state = 805;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 804;
					this.partitionSpec();
					}
				}

				}
				break;

			case 42:
				_localctx = new ShowTblPropertiesContext(_localctx);
				this.enterOuterAlt(_localctx, 42);
				{
				this.state = 807;
				this.match(SparkSqlParser.SHOW);
				this.state = 808;
				this.match(SparkSqlParser.TBLPROPERTIES);
				this.state = 809;
				(_localctx as ShowTblPropertiesContext)._table = this.multipartIdentifier();
				this.state = 814;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 74, this._ctx) ) {
				case 1:
					{
					this.state = 810;
					this.match(SparkSqlParser.T__0);
					this.state = 811;
					(_localctx as ShowTblPropertiesContext)._key = this.tablePropertyKey();
					this.state = 812;
					this.match(SparkSqlParser.T__1);
					}
					break;
				}
				}
				break;

			case 43:
				_localctx = new ShowColumnsContext(_localctx);
				this.enterOuterAlt(_localctx, 43);
				{
				this.state = 816;
				this.match(SparkSqlParser.SHOW);
				this.state = 817;
				this.match(SparkSqlParser.COLUMNS);
				this.state = 818;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.FROM || _la === SparkSqlParser.IN)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 819;
				(_localctx as ShowColumnsContext)._table = this.multipartIdentifier();
				this.state = 822;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 75, this._ctx) ) {
				case 1:
					{
					this.state = 820;
					_la = this._input.LA(1);
					if (!(_la === SparkSqlParser.FROM || _la === SparkSqlParser.IN)) {
					this._errHandler.recoverInline(this);
					} else {
						if (this._input.LA(1) === Token.EOF) {
							this.matchedEOF = true;
						}

						this._errHandler.reportMatch(this);
						this.consume();
					}
					this.state = 821;
					(_localctx as ShowColumnsContext)._ns = this.multipartIdentifier();
					}
					break;
				}
				}
				break;

			case 44:
				_localctx = new ShowViewsContext(_localctx);
				this.enterOuterAlt(_localctx, 44);
				{
				this.state = 824;
				this.match(SparkSqlParser.SHOW);
				this.state = 825;
				this.match(SparkSqlParser.VIEWS);
				this.state = 828;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 76, this._ctx) ) {
				case 1:
					{
					this.state = 826;
					_la = this._input.LA(1);
					if (!(_la === SparkSqlParser.FROM || _la === SparkSqlParser.IN)) {
					this._errHandler.recoverInline(this);
					} else {
						if (this._input.LA(1) === Token.EOF) {
							this.matchedEOF = true;
						}

						this._errHandler.reportMatch(this);
						this.consume();
					}
					this.state = 827;
					this.multipartIdentifier();
					}
					break;
				}
				this.state = 834;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.LIKE || _la === SparkSqlParser.STRING) {
					{
					this.state = 831;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					if (_la === SparkSqlParser.LIKE) {
						{
						this.state = 830;
						this.match(SparkSqlParser.LIKE);
						}
					}

					this.state = 833;
					(_localctx as ShowViewsContext)._pattern = this.match(SparkSqlParser.STRING);
					}
				}

				}
				break;

			case 45:
				_localctx = new ShowPartitionsContext(_localctx);
				this.enterOuterAlt(_localctx, 45);
				{
				this.state = 836;
				this.match(SparkSqlParser.SHOW);
				this.state = 837;
				this.match(SparkSqlParser.PARTITIONS);
				this.state = 838;
				this.multipartIdentifier();
				this.state = 840;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 839;
					this.partitionSpec();
					}
				}

				}
				break;

			case 46:
				_localctx = new ShowFunctionsContext(_localctx);
				this.enterOuterAlt(_localctx, 46);
				{
				this.state = 842;
				this.match(SparkSqlParser.SHOW);
				this.state = 844;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 80, this._ctx) ) {
				case 1:
					{
					this.state = 843;
					this.identifier();
					}
					break;
				}
				this.state = 846;
				this.match(SparkSqlParser.FUNCTIONS);
				this.state = 854;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 83, this._ctx) ) {
				case 1:
					{
					this.state = 848;
					this._errHandler.sync(this);
					switch ( this.interpreter.adaptivePredict(this._input, 81, this._ctx) ) {
					case 1:
						{
						this.state = 847;
						this.match(SparkSqlParser.LIKE);
						}
						break;
					}
					this.state = 852;
					this._errHandler.sync(this);
					switch ( this.interpreter.adaptivePredict(this._input, 82, this._ctx) ) {
					case 1:
						{
						this.state = 850;
						this.multipartIdentifier();
						}
						break;

					case 2:
						{
						this.state = 851;
						(_localctx as ShowFunctionsContext)._pattern = this.match(SparkSqlParser.STRING);
						}
						break;
					}
					}
					break;
				}
				}
				break;

			case 47:
				_localctx = new ShowCreateTableContext(_localctx);
				this.enterOuterAlt(_localctx, 47);
				{
				this.state = 856;
				this.match(SparkSqlParser.SHOW);
				this.state = 857;
				this.match(SparkSqlParser.CREATE);
				this.state = 858;
				this.match(SparkSqlParser.TABLE);
				this.state = 859;
				this.multipartIdentifier();
				this.state = 862;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.AS) {
					{
					this.state = 860;
					this.match(SparkSqlParser.AS);
					this.state = 861;
					this.match(SparkSqlParser.SERDE);
					}
				}

				}
				break;

			case 48:
				_localctx = new ShowCurrentNamespaceContext(_localctx);
				this.enterOuterAlt(_localctx, 48);
				{
				this.state = 864;
				this.match(SparkSqlParser.SHOW);
				this.state = 865;
				this.match(SparkSqlParser.CURRENT);
				this.state = 866;
				this.match(SparkSqlParser.NAMESPACE);
				}
				break;

			case 49:
				_localctx = new DescribeFunctionContext(_localctx);
				this.enterOuterAlt(_localctx, 49);
				{
				this.state = 867;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.DESC || _la === SparkSqlParser.DESCRIBE)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 868;
				this.match(SparkSqlParser.FUNCTION);
				this.state = 870;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 85, this._ctx) ) {
				case 1:
					{
					this.state = 869;
					this.match(SparkSqlParser.EXTENDED);
					}
					break;
				}
				this.state = 872;
				this.describeFuncName();
				}
				break;

			case 50:
				_localctx = new DescribeNamespaceContext(_localctx);
				this.enterOuterAlt(_localctx, 50);
				{
				this.state = 873;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.DESC || _la === SparkSqlParser.DESCRIBE)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 874;
				this.namespace();
				this.state = 876;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 86, this._ctx) ) {
				case 1:
					{
					this.state = 875;
					this.match(SparkSqlParser.EXTENDED);
					}
					break;
				}
				this.state = 878;
				this.multipartIdentifier();
				}
				break;

			case 51:
				_localctx = new DescribeRelationContext(_localctx);
				this.enterOuterAlt(_localctx, 51);
				{
				this.state = 880;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.DESC || _la === SparkSqlParser.DESCRIBE)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 882;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 87, this._ctx) ) {
				case 1:
					{
					this.state = 881;
					this.match(SparkSqlParser.TABLE);
					}
					break;
				}
				this.state = 885;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 88, this._ctx) ) {
				case 1:
					{
					this.state = 884;
					(_localctx as DescribeRelationContext)._option = this._input.LT(1);
					_la = this._input.LA(1);
					if (!(_la === SparkSqlParser.EXTENDED || _la === SparkSqlParser.FORMATTED)) {
						(_localctx as DescribeRelationContext)._option = this._errHandler.recoverInline(this);
					} else {
						if (this._input.LA(1) === Token.EOF) {
							this.matchedEOF = true;
						}

						this._errHandler.reportMatch(this);
						this.consume();
					}
					}
					break;
				}
				this.state = 887;
				this.multipartIdentifier();
				this.state = 889;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 89, this._ctx) ) {
				case 1:
					{
					this.state = 888;
					this.partitionSpec();
					}
					break;
				}
				this.state = 892;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 90, this._ctx) ) {
				case 1:
					{
					this.state = 891;
					this.describeColName();
					}
					break;
				}
				}
				break;

			case 52:
				_localctx = new DescribeQueryContext(_localctx);
				this.enterOuterAlt(_localctx, 52);
				{
				this.state = 894;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.DESC || _la === SparkSqlParser.DESCRIBE)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 896;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.QUERY) {
					{
					this.state = 895;
					this.match(SparkSqlParser.QUERY);
					}
				}

				this.state = 898;
				this.query();
				}
				break;

			case 53:
				_localctx = new CommentNamespaceContext(_localctx);
				this.enterOuterAlt(_localctx, 53);
				{
				this.state = 899;
				this.match(SparkSqlParser.COMMENT);
				this.state = 900;
				this.match(SparkSqlParser.ON);
				this.state = 901;
				this.namespace();
				this.state = 902;
				this.multipartIdentifier();
				this.state = 903;
				this.match(SparkSqlParser.IS);
				this.state = 904;
				(_localctx as CommentNamespaceContext)._comment = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.NULL || _la === SparkSqlParser.STRING)) {
					(_localctx as CommentNamespaceContext)._comment = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
				break;

			case 54:
				_localctx = new CommentTableContext(_localctx);
				this.enterOuterAlt(_localctx, 54);
				{
				this.state = 906;
				this.match(SparkSqlParser.COMMENT);
				this.state = 907;
				this.match(SparkSqlParser.ON);
				this.state = 908;
				this.match(SparkSqlParser.TABLE);
				this.state = 909;
				this.multipartIdentifier();
				this.state = 910;
				this.match(SparkSqlParser.IS);
				this.state = 911;
				(_localctx as CommentTableContext)._comment = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.NULL || _la === SparkSqlParser.STRING)) {
					(_localctx as CommentTableContext)._comment = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
				break;

			case 55:
				_localctx = new RefreshTableContext(_localctx);
				this.enterOuterAlt(_localctx, 55);
				{
				this.state = 913;
				this.match(SparkSqlParser.REFRESH);
				this.state = 914;
				this.match(SparkSqlParser.TABLE);
				this.state = 915;
				this.multipartIdentifier();
				}
				break;

			case 56:
				_localctx = new RefreshFunctionContext(_localctx);
				this.enterOuterAlt(_localctx, 56);
				{
				this.state = 916;
				this.match(SparkSqlParser.REFRESH);
				this.state = 917;
				this.match(SparkSqlParser.FUNCTION);
				this.state = 918;
				this.multipartIdentifier();
				}
				break;

			case 57:
				_localctx = new RefreshResourceContext(_localctx);
				this.enterOuterAlt(_localctx, 57);
				{
				this.state = 919;
				this.match(SparkSqlParser.REFRESH);
				this.state = 927;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 93, this._ctx) ) {
				case 1:
					{
					this.state = 920;
					this.match(SparkSqlParser.STRING);
					}
					break;

				case 2:
					{
					this.state = 924;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 92, this._ctx);
					while (_alt !== 1 && _alt !== ATN.INVALID_ALT_NUMBER) {
						if (_alt === 1 + 1) {
							{
							{
							this.state = 921;
							this.matchWildcard();
							}
							}
						}
						this.state = 926;
						this._errHandler.sync(this);
						_alt = this.interpreter.adaptivePredict(this._input, 92, this._ctx);
					}
					}
					break;
				}
				}
				break;

			case 58:
				_localctx = new CacheTableContext(_localctx);
				this.enterOuterAlt(_localctx, 58);
				{
				this.state = 929;
				this.match(SparkSqlParser.CACHE);
				this.state = 931;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.LAZY) {
					{
					this.state = 930;
					this.match(SparkSqlParser.LAZY);
					}
				}

				this.state = 933;
				this.match(SparkSqlParser.TABLE);
				this.state = 934;
				this.multipartIdentifier();
				this.state = 937;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.OPTIONS) {
					{
					this.state = 935;
					this.match(SparkSqlParser.OPTIONS);
					this.state = 936;
					this.tablePropertyList();
					}
				}

				this.state = 943;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 97, this._ctx) ) {
				case 1:
					{
					this.state = 940;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					if (_la === SparkSqlParser.AS) {
						{
						this.state = 939;
						this.match(SparkSqlParser.AS);
						}
					}

					this.state = 942;
					this.query();
					}
					break;
				}
				}
				break;

			case 59:
				_localctx = new UncacheTableContext(_localctx);
				this.enterOuterAlt(_localctx, 59);
				{
				this.state = 945;
				this.match(SparkSqlParser.UNCACHE);
				this.state = 946;
				this.match(SparkSqlParser.TABLE);
				this.state = 949;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 98, this._ctx) ) {
				case 1:
					{
					this.state = 947;
					this.match(SparkSqlParser.IF);
					this.state = 948;
					this.match(SparkSqlParser.EXISTS);
					}
					break;
				}
				this.state = 951;
				this.multipartIdentifier();
				}
				break;

			case 60:
				_localctx = new ClearCacheContext(_localctx);
				this.enterOuterAlt(_localctx, 60);
				{
				this.state = 952;
				this.match(SparkSqlParser.CLEAR);
				this.state = 953;
				this.match(SparkSqlParser.CACHE);
				}
				break;

			case 61:
				_localctx = new LoadDataContext(_localctx);
				this.enterOuterAlt(_localctx, 61);
				{
				this.state = 954;
				this.match(SparkSqlParser.LOAD);
				this.state = 955;
				this.match(SparkSqlParser.DATA);
				this.state = 957;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.LOCAL) {
					{
					this.state = 956;
					this.match(SparkSqlParser.LOCAL);
					}
				}

				this.state = 959;
				this.match(SparkSqlParser.INPATH);
				this.state = 960;
				(_localctx as LoadDataContext)._path = this.match(SparkSqlParser.STRING);
				this.state = 962;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.OVERWRITE) {
					{
					this.state = 961;
					this.match(SparkSqlParser.OVERWRITE);
					}
				}

				this.state = 964;
				this.match(SparkSqlParser.INTO);
				this.state = 965;
				this.match(SparkSqlParser.TABLE);
				this.state = 966;
				this.multipartIdentifier();
				this.state = 968;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 967;
					this.partitionSpec();
					}
				}

				}
				break;

			case 62:
				_localctx = new TruncateTableContext(_localctx);
				this.enterOuterAlt(_localctx, 62);
				{
				this.state = 970;
				this.match(SparkSqlParser.TRUNCATE);
				this.state = 971;
				this.match(SparkSqlParser.TABLE);
				this.state = 972;
				this.multipartIdentifier();
				this.state = 974;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 973;
					this.partitionSpec();
					}
				}

				}
				break;

			case 63:
				_localctx = new RepairTableContext(_localctx);
				this.enterOuterAlt(_localctx, 63);
				{
				this.state = 976;
				this.match(SparkSqlParser.MSCK);
				this.state = 977;
				this.match(SparkSqlParser.REPAIR);
				this.state = 978;
				this.match(SparkSqlParser.TABLE);
				this.state = 979;
				this.multipartIdentifier();
				}
				break;

			case 64:
				_localctx = new ManageResourceContext(_localctx);
				this.enterOuterAlt(_localctx, 64);
				{
				this.state = 980;
				(_localctx as ManageResourceContext)._op = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.ADD || _la === SparkSqlParser.LIST)) {
					(_localctx as ManageResourceContext)._op = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 981;
				this.identifier();
				this.state = 989;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 104, this._ctx) ) {
				case 1:
					{
					this.state = 982;
					this.match(SparkSqlParser.STRING);
					}
					break;

				case 2:
					{
					this.state = 986;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 103, this._ctx);
					while (_alt !== 1 && _alt !== ATN.INVALID_ALT_NUMBER) {
						if (_alt === 1 + 1) {
							{
							{
							this.state = 983;
							this.matchWildcard();
							}
							}
						}
						this.state = 988;
						this._errHandler.sync(this);
						_alt = this.interpreter.adaptivePredict(this._input, 103, this._ctx);
					}
					}
					break;
				}
				}
				break;

			case 65:
				_localctx = new FailNativeCommandContext(_localctx);
				this.enterOuterAlt(_localctx, 65);
				{
				this.state = 991;
				this.match(SparkSqlParser.SET);
				this.state = 992;
				this.match(SparkSqlParser.ROLE);
				this.state = 996;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 105, this._ctx);
				while (_alt !== 1 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1 + 1) {
						{
						{
						this.state = 993;
						this.matchWildcard();
						}
						}
					}
					this.state = 998;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 105, this._ctx);
				}
				}
				break;

			case 66:
				_localctx = new SetTimeZoneContext(_localctx);
				this.enterOuterAlt(_localctx, 66);
				{
				this.state = 999;
				this.match(SparkSqlParser.SET);
				this.state = 1000;
				this.match(SparkSqlParser.TIME);
				this.state = 1001;
				this.match(SparkSqlParser.ZONE);
				this.state = 1002;
				this.interval();
				}
				break;

			case 67:
				_localctx = new SetTimeZoneContext(_localctx);
				this.enterOuterAlt(_localctx, 67);
				{
				this.state = 1003;
				this.match(SparkSqlParser.SET);
				this.state = 1004;
				this.match(SparkSqlParser.TIME);
				this.state = 1005;
				this.match(SparkSqlParser.ZONE);
				this.state = 1006;
				(_localctx as SetTimeZoneContext)._timezone = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.LOCAL || _la === SparkSqlParser.STRING)) {
					(_localctx as SetTimeZoneContext)._timezone = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
				break;

			case 68:
				_localctx = new SetTimeZoneContext(_localctx);
				this.enterOuterAlt(_localctx, 68);
				{
				this.state = 1007;
				this.match(SparkSqlParser.SET);
				this.state = 1008;
				this.match(SparkSqlParser.TIME);
				this.state = 1009;
				this.match(SparkSqlParser.ZONE);
				this.state = 1013;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 106, this._ctx);
				while (_alt !== 1 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1 + 1) {
						{
						{
						this.state = 1010;
						this.matchWildcard();
						}
						}
					}
					this.state = 1015;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 106, this._ctx);
				}
				}
				break;

			case 69:
				_localctx = new SetQuotedConfigurationContext(_localctx);
				this.enterOuterAlt(_localctx, 69);
				{
				this.state = 1016;
				this.match(SparkSqlParser.SET);
				this.state = 1017;
				this.configKey();
				this.state = 1025;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.EQ) {
					{
					this.state = 1018;
					this.match(SparkSqlParser.EQ);
					this.state = 1022;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 107, this._ctx);
					while (_alt !== 1 && _alt !== ATN.INVALID_ALT_NUMBER) {
						if (_alt === 1 + 1) {
							{
							{
							this.state = 1019;
							this.matchWildcard();
							}
							}
						}
						this.state = 1024;
						this._errHandler.sync(this);
						_alt = this.interpreter.adaptivePredict(this._input, 107, this._ctx);
					}
					}
				}

				}
				break;

			case 70:
				_localctx = new SetConfigurationContext(_localctx);
				this.enterOuterAlt(_localctx, 70);
				{
				this.state = 1027;
				this.match(SparkSqlParser.SET);
				this.state = 1031;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 109, this._ctx);
				while (_alt !== 1 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1 + 1) {
						{
						{
						this.state = 1028;
						this.matchWildcard();
						}
						}
					}
					this.state = 1033;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 109, this._ctx);
				}
				}
				break;

			case 71:
				_localctx = new ResetQuotedConfigurationContext(_localctx);
				this.enterOuterAlt(_localctx, 71);
				{
				this.state = 1034;
				this.match(SparkSqlParser.RESET);
				this.state = 1035;
				this.configKey();
				}
				break;

			case 72:
				_localctx = new ResetConfigurationContext(_localctx);
				this.enterOuterAlt(_localctx, 72);
				{
				this.state = 1036;
				this.match(SparkSqlParser.RESET);
				this.state = 1040;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 110, this._ctx);
				while (_alt !== 1 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1 + 1) {
						{
						{
						this.state = 1037;
						this.matchWildcard();
						}
						}
					}
					this.state = 1042;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 110, this._ctx);
				}
				}
				break;

			case 73:
				_localctx = new FailNativeCommandContext(_localctx);
				this.enterOuterAlt(_localctx, 73);
				{
				this.state = 1043;
				this.unsupportedHiveNativeCommands();
				this.state = 1047;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 111, this._ctx);
				while (_alt !== 1 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1 + 1) {
						{
						{
						this.state = 1044;
						this.matchWildcard();
						}
						}
					}
					this.state = 1049;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 111, this._ctx);
				}
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public configKey(): ConfigKeyContext {
		let _localctx: ConfigKeyContext = new ConfigKeyContext(this._ctx, this.state);
		this.enterRule(_localctx, 18, SparkSqlParser.RULE_configKey);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1052;
			this.quotedIdentifier();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public unsupportedHiveNativeCommands(): UnsupportedHiveNativeCommandsContext {
		let _localctx: UnsupportedHiveNativeCommandsContext = new UnsupportedHiveNativeCommandsContext(this._ctx, this.state);
		this.enterRule(_localctx, 20, SparkSqlParser.RULE_unsupportedHiveNativeCommands);
		let _la: number;
		try {
			this.state = 1222;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 120, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1054;
				_localctx._kw1 = this.match(SparkSqlParser.CREATE);
				this.state = 1055;
				_localctx._kw2 = this.match(SparkSqlParser.ROLE);
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1056;
				_localctx._kw1 = this.match(SparkSqlParser.DROP);
				this.state = 1057;
				_localctx._kw2 = this.match(SparkSqlParser.ROLE);
				}
				break;

			case 3:
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 1058;
				_localctx._kw1 = this.match(SparkSqlParser.GRANT);
				this.state = 1060;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 113, this._ctx) ) {
				case 1:
					{
					this.state = 1059;
					_localctx._kw2 = this.match(SparkSqlParser.ROLE);
					}
					break;
				}
				}
				break;

			case 4:
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 1062;
				_localctx._kw1 = this.match(SparkSqlParser.REVOKE);
				this.state = 1064;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 114, this._ctx) ) {
				case 1:
					{
					this.state = 1063;
					_localctx._kw2 = this.match(SparkSqlParser.ROLE);
					}
					break;
				}
				}
				break;

			case 5:
				this.enterOuterAlt(_localctx, 5);
				{
				this.state = 1066;
				_localctx._kw1 = this.match(SparkSqlParser.SHOW);
				this.state = 1067;
				_localctx._kw2 = this.match(SparkSqlParser.GRANT);
				}
				break;

			case 6:
				this.enterOuterAlt(_localctx, 6);
				{
				this.state = 1068;
				_localctx._kw1 = this.match(SparkSqlParser.SHOW);
				this.state = 1069;
				_localctx._kw2 = this.match(SparkSqlParser.ROLE);
				this.state = 1071;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 115, this._ctx) ) {
				case 1:
					{
					this.state = 1070;
					_localctx._kw3 = this.match(SparkSqlParser.GRANT);
					}
					break;
				}
				}
				break;

			case 7:
				this.enterOuterAlt(_localctx, 7);
				{
				this.state = 1073;
				_localctx._kw1 = this.match(SparkSqlParser.SHOW);
				this.state = 1074;
				_localctx._kw2 = this.match(SparkSqlParser.PRINCIPALS);
				}
				break;

			case 8:
				this.enterOuterAlt(_localctx, 8);
				{
				this.state = 1075;
				_localctx._kw1 = this.match(SparkSqlParser.SHOW);
				this.state = 1076;
				_localctx._kw2 = this.match(SparkSqlParser.ROLES);
				}
				break;

			case 9:
				this.enterOuterAlt(_localctx, 9);
				{
				this.state = 1077;
				_localctx._kw1 = this.match(SparkSqlParser.SHOW);
				this.state = 1078;
				_localctx._kw2 = this.match(SparkSqlParser.CURRENT);
				this.state = 1079;
				_localctx._kw3 = this.match(SparkSqlParser.ROLES);
				}
				break;

			case 10:
				this.enterOuterAlt(_localctx, 10);
				{
				this.state = 1080;
				_localctx._kw1 = this.match(SparkSqlParser.EXPORT);
				this.state = 1081;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				}
				break;

			case 11:
				this.enterOuterAlt(_localctx, 11);
				{
				this.state = 1082;
				_localctx._kw1 = this.match(SparkSqlParser.IMPORT);
				this.state = 1083;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				}
				break;

			case 12:
				this.enterOuterAlt(_localctx, 12);
				{
				this.state = 1084;
				_localctx._kw1 = this.match(SparkSqlParser.SHOW);
				this.state = 1085;
				_localctx._kw2 = this.match(SparkSqlParser.COMPACTIONS);
				}
				break;

			case 13:
				this.enterOuterAlt(_localctx, 13);
				{
				this.state = 1086;
				_localctx._kw1 = this.match(SparkSqlParser.SHOW);
				this.state = 1087;
				_localctx._kw2 = this.match(SparkSqlParser.CREATE);
				this.state = 1088;
				_localctx._kw3 = this.match(SparkSqlParser.TABLE);
				}
				break;

			case 14:
				this.enterOuterAlt(_localctx, 14);
				{
				this.state = 1089;
				_localctx._kw1 = this.match(SparkSqlParser.SHOW);
				this.state = 1090;
				_localctx._kw2 = this.match(SparkSqlParser.TRANSACTIONS);
				}
				break;

			case 15:
				this.enterOuterAlt(_localctx, 15);
				{
				this.state = 1091;
				_localctx._kw1 = this.match(SparkSqlParser.SHOW);
				this.state = 1092;
				_localctx._kw2 = this.match(SparkSqlParser.INDEXES);
				}
				break;

			case 16:
				this.enterOuterAlt(_localctx, 16);
				{
				this.state = 1093;
				_localctx._kw1 = this.match(SparkSqlParser.SHOW);
				this.state = 1094;
				_localctx._kw2 = this.match(SparkSqlParser.LOCKS);
				}
				break;

			case 17:
				this.enterOuterAlt(_localctx, 17);
				{
				this.state = 1095;
				_localctx._kw1 = this.match(SparkSqlParser.CREATE);
				this.state = 1096;
				_localctx._kw2 = this.match(SparkSqlParser.INDEX);
				}
				break;

			case 18:
				this.enterOuterAlt(_localctx, 18);
				{
				this.state = 1097;
				_localctx._kw1 = this.match(SparkSqlParser.DROP);
				this.state = 1098;
				_localctx._kw2 = this.match(SparkSqlParser.INDEX);
				}
				break;

			case 19:
				this.enterOuterAlt(_localctx, 19);
				{
				this.state = 1099;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1100;
				_localctx._kw2 = this.match(SparkSqlParser.INDEX);
				}
				break;

			case 20:
				this.enterOuterAlt(_localctx, 20);
				{
				this.state = 1101;
				_localctx._kw1 = this.match(SparkSqlParser.LOCK);
				this.state = 1102;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				}
				break;

			case 21:
				this.enterOuterAlt(_localctx, 21);
				{
				this.state = 1103;
				_localctx._kw1 = this.match(SparkSqlParser.LOCK);
				this.state = 1104;
				_localctx._kw2 = this.match(SparkSqlParser.DATABASE);
				}
				break;

			case 22:
				this.enterOuterAlt(_localctx, 22);
				{
				this.state = 1105;
				_localctx._kw1 = this.match(SparkSqlParser.UNLOCK);
				this.state = 1106;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				}
				break;

			case 23:
				this.enterOuterAlt(_localctx, 23);
				{
				this.state = 1107;
				_localctx._kw1 = this.match(SparkSqlParser.UNLOCK);
				this.state = 1108;
				_localctx._kw2 = this.match(SparkSqlParser.DATABASE);
				}
				break;

			case 24:
				this.enterOuterAlt(_localctx, 24);
				{
				this.state = 1109;
				_localctx._kw1 = this.match(SparkSqlParser.CREATE);
				this.state = 1110;
				_localctx._kw2 = this.match(SparkSqlParser.TEMPORARY);
				this.state = 1111;
				_localctx._kw3 = this.match(SparkSqlParser.MACRO);
				}
				break;

			case 25:
				this.enterOuterAlt(_localctx, 25);
				{
				this.state = 1112;
				_localctx._kw1 = this.match(SparkSqlParser.DROP);
				this.state = 1113;
				_localctx._kw2 = this.match(SparkSqlParser.TEMPORARY);
				this.state = 1114;
				_localctx._kw3 = this.match(SparkSqlParser.MACRO);
				}
				break;

			case 26:
				this.enterOuterAlt(_localctx, 26);
				{
				this.state = 1115;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1116;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1117;
				this.tableIdentifier();
				this.state = 1118;
				_localctx._kw3 = this.match(SparkSqlParser.NOT);
				this.state = 1119;
				_localctx._kw4 = this.match(SparkSqlParser.CLUSTERED);
				}
				break;

			case 27:
				this.enterOuterAlt(_localctx, 27);
				{
				this.state = 1121;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1122;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1123;
				this.tableIdentifier();
				this.state = 1124;
				_localctx._kw3 = this.match(SparkSqlParser.CLUSTERED);
				this.state = 1125;
				_localctx._kw4 = this.match(SparkSqlParser.BY);
				}
				break;

			case 28:
				this.enterOuterAlt(_localctx, 28);
				{
				this.state = 1127;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1128;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1129;
				this.tableIdentifier();
				this.state = 1130;
				_localctx._kw3 = this.match(SparkSqlParser.NOT);
				this.state = 1131;
				_localctx._kw4 = this.match(SparkSqlParser.SORTED);
				}
				break;

			case 29:
				this.enterOuterAlt(_localctx, 29);
				{
				this.state = 1133;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1134;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1135;
				this.tableIdentifier();
				this.state = 1136;
				_localctx._kw3 = this.match(SparkSqlParser.SKEWED);
				this.state = 1137;
				_localctx._kw4 = this.match(SparkSqlParser.BY);
				}
				break;

			case 30:
				this.enterOuterAlt(_localctx, 30);
				{
				this.state = 1139;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1140;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1141;
				this.tableIdentifier();
				this.state = 1142;
				_localctx._kw3 = this.match(SparkSqlParser.NOT);
				this.state = 1143;
				_localctx._kw4 = this.match(SparkSqlParser.SKEWED);
				}
				break;

			case 31:
				this.enterOuterAlt(_localctx, 31);
				{
				this.state = 1145;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1146;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1147;
				this.tableIdentifier();
				this.state = 1148;
				_localctx._kw3 = this.match(SparkSqlParser.NOT);
				this.state = 1149;
				_localctx._kw4 = this.match(SparkSqlParser.STORED);
				this.state = 1150;
				_localctx._kw5 = this.match(SparkSqlParser.AS);
				this.state = 1151;
				_localctx._kw6 = this.match(SparkSqlParser.DIRECTORIES);
				}
				break;

			case 32:
				this.enterOuterAlt(_localctx, 32);
				{
				this.state = 1153;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1154;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1155;
				this.tableIdentifier();
				this.state = 1156;
				_localctx._kw3 = this.match(SparkSqlParser.SET);
				this.state = 1157;
				_localctx._kw4 = this.match(SparkSqlParser.SKEWED);
				this.state = 1158;
				_localctx._kw5 = this.match(SparkSqlParser.LOCATION);
				}
				break;

			case 33:
				this.enterOuterAlt(_localctx, 33);
				{
				this.state = 1160;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1161;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1162;
				this.tableIdentifier();
				this.state = 1163;
				_localctx._kw3 = this.match(SparkSqlParser.EXCHANGE);
				this.state = 1164;
				_localctx._kw4 = this.match(SparkSqlParser.PARTITION);
				}
				break;

			case 34:
				this.enterOuterAlt(_localctx, 34);
				{
				this.state = 1166;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1167;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1168;
				this.tableIdentifier();
				this.state = 1169;
				_localctx._kw3 = this.match(SparkSqlParser.ARCHIVE);
				this.state = 1170;
				_localctx._kw4 = this.match(SparkSqlParser.PARTITION);
				}
				break;

			case 35:
				this.enterOuterAlt(_localctx, 35);
				{
				this.state = 1172;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1173;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1174;
				this.tableIdentifier();
				this.state = 1175;
				_localctx._kw3 = this.match(SparkSqlParser.UNARCHIVE);
				this.state = 1176;
				_localctx._kw4 = this.match(SparkSqlParser.PARTITION);
				}
				break;

			case 36:
				this.enterOuterAlt(_localctx, 36);
				{
				this.state = 1178;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1179;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1180;
				this.tableIdentifier();
				this.state = 1181;
				_localctx._kw3 = this.match(SparkSqlParser.TOUCH);
				}
				break;

			case 37:
				this.enterOuterAlt(_localctx, 37);
				{
				this.state = 1183;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1184;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1185;
				this.tableIdentifier();
				this.state = 1187;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 1186;
					this.partitionSpec();
					}
				}

				this.state = 1189;
				_localctx._kw3 = this.match(SparkSqlParser.COMPACT);
				}
				break;

			case 38:
				this.enterOuterAlt(_localctx, 38);
				{
				this.state = 1191;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1192;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1193;
				this.tableIdentifier();
				this.state = 1195;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 1194;
					this.partitionSpec();
					}
				}

				this.state = 1197;
				_localctx._kw3 = this.match(SparkSqlParser.CONCATENATE);
				}
				break;

			case 39:
				this.enterOuterAlt(_localctx, 39);
				{
				this.state = 1199;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1200;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1201;
				this.tableIdentifier();
				this.state = 1203;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 1202;
					this.partitionSpec();
					}
				}

				this.state = 1205;
				_localctx._kw3 = this.match(SparkSqlParser.SET);
				this.state = 1206;
				_localctx._kw4 = this.match(SparkSqlParser.FILEFORMAT);
				}
				break;

			case 40:
				this.enterOuterAlt(_localctx, 40);
				{
				this.state = 1208;
				_localctx._kw1 = this.match(SparkSqlParser.ALTER);
				this.state = 1209;
				_localctx._kw2 = this.match(SparkSqlParser.TABLE);
				this.state = 1210;
				this.tableIdentifier();
				this.state = 1212;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 1211;
					this.partitionSpec();
					}
				}

				this.state = 1214;
				_localctx._kw3 = this.match(SparkSqlParser.REPLACE);
				this.state = 1215;
				_localctx._kw4 = this.match(SparkSqlParser.COLUMNS);
				}
				break;

			case 41:
				this.enterOuterAlt(_localctx, 41);
				{
				this.state = 1217;
				_localctx._kw1 = this.match(SparkSqlParser.START);
				this.state = 1218;
				_localctx._kw2 = this.match(SparkSqlParser.TRANSACTION);
				}
				break;

			case 42:
				this.enterOuterAlt(_localctx, 42);
				{
				this.state = 1219;
				_localctx._kw1 = this.match(SparkSqlParser.COMMIT);
				}
				break;

			case 43:
				this.enterOuterAlt(_localctx, 43);
				{
				this.state = 1220;
				_localctx._kw1 = this.match(SparkSqlParser.ROLLBACK);
				}
				break;

			case 44:
				this.enterOuterAlt(_localctx, 44);
				{
				this.state = 1221;
				_localctx._kw1 = this.match(SparkSqlParser.DFS);
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public createTableHeader(): CreateTableHeaderContext {
		let _localctx: CreateTableHeaderContext = new CreateTableHeaderContext(this._ctx, this.state);
		this.enterRule(_localctx, 22, SparkSqlParser.RULE_createTableHeader);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1224;
			this.match(SparkSqlParser.CREATE);
			this.state = 1226;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.TEMPORARY) {
				{
				this.state = 1225;
				this.match(SparkSqlParser.TEMPORARY);
				}
			}

			this.state = 1229;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.EXTERNAL) {
				{
				this.state = 1228;
				this.match(SparkSqlParser.EXTERNAL);
				}
			}

			this.state = 1231;
			this.match(SparkSqlParser.TABLE);
			this.state = 1235;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 123, this._ctx) ) {
			case 1:
				{
				this.state = 1232;
				this.match(SparkSqlParser.IF);
				this.state = 1233;
				this.match(SparkSqlParser.NOT);
				this.state = 1234;
				this.match(SparkSqlParser.EXISTS);
				}
				break;
			}
			this.state = 1237;
			this.multipartIdentifier();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public replaceTableHeader(): ReplaceTableHeaderContext {
		let _localctx: ReplaceTableHeaderContext = new ReplaceTableHeaderContext(this._ctx, this.state);
		this.enterRule(_localctx, 24, SparkSqlParser.RULE_replaceTableHeader);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1241;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.CREATE) {
				{
				this.state = 1239;
				this.match(SparkSqlParser.CREATE);
				this.state = 1240;
				this.match(SparkSqlParser.OR);
				}
			}

			this.state = 1243;
			this.match(SparkSqlParser.REPLACE);
			this.state = 1244;
			this.match(SparkSqlParser.TABLE);
			this.state = 1245;
			this.multipartIdentifier();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public bucketSpec(): BucketSpecContext {
		let _localctx: BucketSpecContext = new BucketSpecContext(this._ctx, this.state);
		this.enterRule(_localctx, 26, SparkSqlParser.RULE_bucketSpec);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1247;
			this.match(SparkSqlParser.CLUSTERED);
			this.state = 1248;
			this.match(SparkSqlParser.BY);
			this.state = 1249;
			this.identifierList();
			this.state = 1253;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.SORTED) {
				{
				this.state = 1250;
				this.match(SparkSqlParser.SORTED);
				this.state = 1251;
				this.match(SparkSqlParser.BY);
				this.state = 1252;
				this.orderedIdentifierList();
				}
			}

			this.state = 1255;
			this.match(SparkSqlParser.INTO);
			this.state = 1256;
			this.match(SparkSqlParser.INTEGER_VALUE);
			this.state = 1257;
			this.match(SparkSqlParser.BUCKETS);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public skewSpec(): SkewSpecContext {
		let _localctx: SkewSpecContext = new SkewSpecContext(this._ctx, this.state);
		this.enterRule(_localctx, 28, SparkSqlParser.RULE_skewSpec);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1259;
			this.match(SparkSqlParser.SKEWED);
			this.state = 1260;
			this.match(SparkSqlParser.BY);
			this.state = 1261;
			this.identifierList();
			this.state = 1262;
			this.match(SparkSqlParser.ON);
			this.state = 1265;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 126, this._ctx) ) {
			case 1:
				{
				this.state = 1263;
				this.constantList();
				}
				break;

			case 2:
				{
				this.state = 1264;
				this.nestedConstantList();
				}
				break;
			}
			this.state = 1270;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 127, this._ctx) ) {
			case 1:
				{
				this.state = 1267;
				this.match(SparkSqlParser.STORED);
				this.state = 1268;
				this.match(SparkSqlParser.AS);
				this.state = 1269;
				this.match(SparkSqlParser.DIRECTORIES);
				}
				break;
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public locationSpec(): LocationSpecContext {
		let _localctx: LocationSpecContext = new LocationSpecContext(this._ctx, this.state);
		this.enterRule(_localctx, 30, SparkSqlParser.RULE_locationSpec);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1272;
			this.match(SparkSqlParser.LOCATION);
			this.state = 1273;
			this.match(SparkSqlParser.STRING);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public commentSpec(): CommentSpecContext {
		let _localctx: CommentSpecContext = new CommentSpecContext(this._ctx, this.state);
		this.enterRule(_localctx, 32, SparkSqlParser.RULE_commentSpec);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1275;
			this.match(SparkSqlParser.COMMENT);
			this.state = 1276;
			this.match(SparkSqlParser.STRING);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public query(): QueryContext {
		let _localctx: QueryContext = new QueryContext(this._ctx, this.state);
		this.enterRule(_localctx, 34, SparkSqlParser.RULE_query);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1279;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.WITH) {
				{
				this.state = 1278;
				this.ctes();
				}
			}

			this.state = 1281;
			this.queryTerm(0);
			this.state = 1282;
			this.queryOrganization();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public insertInto(): InsertIntoContext {
		let _localctx: InsertIntoContext = new InsertIntoContext(this._ctx, this.state);
		this.enterRule(_localctx, 36, SparkSqlParser.RULE_insertInto);
		let _la: number;
		try {
			this.state = 1339;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 141, this._ctx) ) {
			case 1:
				_localctx = new InsertOverwriteTableContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1284;
				this.match(SparkSqlParser.INSERT);
				this.state = 1285;
				this.match(SparkSqlParser.OVERWRITE);
				this.state = 1287;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 129, this._ctx) ) {
				case 1:
					{
					this.state = 1286;
					this.match(SparkSqlParser.TABLE);
					}
					break;
				}
				this.state = 1289;
				this.multipartIdentifier();
				this.state = 1296;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 1290;
					this.partitionSpec();
					this.state = 1294;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					if (_la === SparkSqlParser.IF) {
						{
						this.state = 1291;
						this.match(SparkSqlParser.IF);
						this.state = 1292;
						this.match(SparkSqlParser.NOT);
						this.state = 1293;
						this.match(SparkSqlParser.EXISTS);
						}
					}

					}
				}

				}
				break;

			case 2:
				_localctx = new InsertIntoTableContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1298;
				this.match(SparkSqlParser.INSERT);
				this.state = 1299;
				this.match(SparkSqlParser.INTO);
				this.state = 1301;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 132, this._ctx) ) {
				case 1:
					{
					this.state = 1300;
					this.match(SparkSqlParser.TABLE);
					}
					break;
				}
				this.state = 1303;
				this.multipartIdentifier();
				this.state = 1305;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PARTITION) {
					{
					this.state = 1304;
					this.partitionSpec();
					}
				}

				this.state = 1310;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.IF) {
					{
					this.state = 1307;
					this.match(SparkSqlParser.IF);
					this.state = 1308;
					this.match(SparkSqlParser.NOT);
					this.state = 1309;
					this.match(SparkSqlParser.EXISTS);
					}
				}

				}
				break;

			case 3:
				_localctx = new InsertOverwriteHiveDirContext(_localctx);
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 1312;
				this.match(SparkSqlParser.INSERT);
				this.state = 1313;
				this.match(SparkSqlParser.OVERWRITE);
				this.state = 1315;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.LOCAL) {
					{
					this.state = 1314;
					this.match(SparkSqlParser.LOCAL);
					}
				}

				this.state = 1317;
				this.match(SparkSqlParser.DIRECTORY);
				this.state = 1318;
				(_localctx as InsertOverwriteHiveDirContext)._path = this.match(SparkSqlParser.STRING);
				this.state = 1320;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.ROW) {
					{
					this.state = 1319;
					this.rowFormat();
					}
				}

				this.state = 1323;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.STORED) {
					{
					this.state = 1322;
					this.createFileFormat();
					}
				}

				}
				break;

			case 4:
				_localctx = new InsertOverwriteDirContext(_localctx);
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 1325;
				this.match(SparkSqlParser.INSERT);
				this.state = 1326;
				this.match(SparkSqlParser.OVERWRITE);
				this.state = 1328;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.LOCAL) {
					{
					this.state = 1327;
					this.match(SparkSqlParser.LOCAL);
					}
				}

				this.state = 1330;
				this.match(SparkSqlParser.DIRECTORY);
				this.state = 1332;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.STRING) {
					{
					this.state = 1331;
					(_localctx as InsertOverwriteDirContext)._path = this.match(SparkSqlParser.STRING);
					}
				}

				this.state = 1334;
				this.tableProvider();
				this.state = 1337;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.OPTIONS) {
					{
					this.state = 1335;
					this.match(SparkSqlParser.OPTIONS);
					this.state = 1336;
					this.tablePropertyList();
					}
				}

				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public partitionSpecLocation(): PartitionSpecLocationContext {
		let _localctx: PartitionSpecLocationContext = new PartitionSpecLocationContext(this._ctx, this.state);
		this.enterRule(_localctx, 38, SparkSqlParser.RULE_partitionSpecLocation);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1341;
			this.partitionSpec();
			this.state = 1343;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.LOCATION) {
				{
				this.state = 1342;
				this.locationSpec();
				}
			}

			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public partitionSpec(): PartitionSpecContext {
		let _localctx: PartitionSpecContext = new PartitionSpecContext(this._ctx, this.state);
		this.enterRule(_localctx, 40, SparkSqlParser.RULE_partitionSpec);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1345;
			this.match(SparkSqlParser.PARTITION);
			this.state = 1346;
			this.match(SparkSqlParser.T__0);
			this.state = 1347;
			this.partitionVal();
			this.state = 1352;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__2) {
				{
				{
				this.state = 1348;
				this.match(SparkSqlParser.T__2);
				this.state = 1349;
				this.partitionVal();
				}
				}
				this.state = 1354;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 1355;
			this.match(SparkSqlParser.T__1);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public partitionVal(): PartitionValContext {
		let _localctx: PartitionValContext = new PartitionValContext(this._ctx, this.state);
		this.enterRule(_localctx, 42, SparkSqlParser.RULE_partitionVal);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1357;
			this.identifier();
			this.state = 1360;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.EQ) {
				{
				this.state = 1358;
				this.match(SparkSqlParser.EQ);
				this.state = 1359;
				this.constant();
				}
			}

			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public namespace(): NamespaceContext {
		let _localctx: NamespaceContext = new NamespaceContext(this._ctx, this.state);
		this.enterRule(_localctx, 44, SparkSqlParser.RULE_namespace);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1362;
			_la = this._input.LA(1);
			if (!(_la === SparkSqlParser.DATABASE || _la === SparkSqlParser.NAMESPACE || _la === SparkSqlParser.SCHEMA)) {
			this._errHandler.recoverInline(this);
			} else {
				if (this._input.LA(1) === Token.EOF) {
					this.matchedEOF = true;
				}

				this._errHandler.reportMatch(this);
				this.consume();
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public describeFuncName(): DescribeFuncNameContext {
		let _localctx: DescribeFuncNameContext = new DescribeFuncNameContext(this._ctx, this.state);
		this.enterRule(_localctx, 46, SparkSqlParser.RULE_describeFuncName);
		try {
			this.state = 1369;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 145, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1364;
				this.qualifiedName();
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1365;
				this.match(SparkSqlParser.STRING);
				}
				break;

			case 3:
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 1366;
				this.comparisonOperator();
				}
				break;

			case 4:
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 1367;
				this.arithmeticOperator();
				}
				break;

			case 5:
				this.enterOuterAlt(_localctx, 5);
				{
				this.state = 1368;
				this.predicateOperator();
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public describeColName(): DescribeColNameContext {
		let _localctx: DescribeColNameContext = new DescribeColNameContext(this._ctx, this.state);
		this.enterRule(_localctx, 48, SparkSqlParser.RULE_describeColName);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1371;
			_localctx._identifier = this.identifier();
			_localctx._nameParts.push(_localctx._identifier);
			this.state = 1376;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__3) {
				{
				{
				this.state = 1372;
				this.match(SparkSqlParser.T__3);
				this.state = 1373;
				_localctx._identifier = this.identifier();
				_localctx._nameParts.push(_localctx._identifier);
				}
				}
				this.state = 1378;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public ctes(): CtesContext {
		let _localctx: CtesContext = new CtesContext(this._ctx, this.state);
		this.enterRule(_localctx, 50, SparkSqlParser.RULE_ctes);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1379;
			this.match(SparkSqlParser.WITH);
			this.state = 1380;
			this.namedQuery();
			this.state = 1385;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__2) {
				{
				{
				this.state = 1381;
				this.match(SparkSqlParser.T__2);
				this.state = 1382;
				this.namedQuery();
				}
				}
				this.state = 1387;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public namedQuery(): NamedQueryContext {
		let _localctx: NamedQueryContext = new NamedQueryContext(this._ctx, this.state);
		this.enterRule(_localctx, 52, SparkSqlParser.RULE_namedQuery);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1388;
			_localctx._name = this.errorCapturingIdentifier();
			this.state = 1390;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 148, this._ctx) ) {
			case 1:
				{
				this.state = 1389;
				_localctx._columnAliases = this.identifierList();
				}
				break;
			}
			this.state = 1393;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.AS) {
				{
				this.state = 1392;
				this.match(SparkSqlParser.AS);
				}
			}

			this.state = 1395;
			this.match(SparkSqlParser.T__0);
			this.state = 1396;
			this.query();
			this.state = 1397;
			this.match(SparkSqlParser.T__1);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public tableProvider(): TableProviderContext {
		let _localctx: TableProviderContext = new TableProviderContext(this._ctx, this.state);
		this.enterRule(_localctx, 54, SparkSqlParser.RULE_tableProvider);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1399;
			this.match(SparkSqlParser.USING);
			this.state = 1400;
			this.multipartIdentifier();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public createTableClauses(): CreateTableClausesContext {
		let _localctx: CreateTableClausesContext = new CreateTableClausesContext(this._ctx, this.state);
		this.enterRule(_localctx, 56, SparkSqlParser.RULE_createTableClauses);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1414;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 151, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					{
					this.state = 1412;
					this._errHandler.sync(this);
					switch (this._input.LA(1)) {
					case SparkSqlParser.OPTIONS:
						{
						{
						this.state = 1402;
						this.match(SparkSqlParser.OPTIONS);
						this.state = 1403;
						this.tablePropertyList();
						}
						}
						break;
					case SparkSqlParser.PARTITIONED:
						{
						{
						this.state = 1404;
						this.match(SparkSqlParser.PARTITIONED);
						this.state = 1405;
						this.match(SparkSqlParser.BY);
						this.state = 1406;
						_localctx._partitioning = this.transformList();
						}
						}
						break;
					case SparkSqlParser.CLUSTERED:
						{
						this.state = 1407;
						this.bucketSpec();
						}
						break;
					case SparkSqlParser.LOCATION:
						{
						this.state = 1408;
						this.locationSpec();
						}
						break;
					case SparkSqlParser.COMMENT:
						{
						this.state = 1409;
						this.commentSpec();
						}
						break;
					case SparkSqlParser.TBLPROPERTIES:
						{
						{
						this.state = 1410;
						this.match(SparkSqlParser.TBLPROPERTIES);
						this.state = 1411;
						_localctx._tableProps = this.tablePropertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
				}
				this.state = 1416;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 151, this._ctx);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public tablePropertyList(): TablePropertyListContext {
		let _localctx: TablePropertyListContext = new TablePropertyListContext(this._ctx, this.state);
		this.enterRule(_localctx, 58, SparkSqlParser.RULE_tablePropertyList);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1417;
			this.match(SparkSqlParser.T__0);
			this.state = 1418;
			this.tableProperty();
			this.state = 1423;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__2) {
				{
				{
				this.state = 1419;
				this.match(SparkSqlParser.T__2);
				this.state = 1420;
				this.tableProperty();
				}
				}
				this.state = 1425;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 1426;
			this.match(SparkSqlParser.T__1);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public tableProperty(): TablePropertyContext {
		let _localctx: TablePropertyContext = new TablePropertyContext(this._ctx, this.state);
		this.enterRule(_localctx, 60, SparkSqlParser.RULE_tableProperty);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1428;
			_localctx._key = this.tablePropertyKey();
			this.state = 1433;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.FALSE || _la === SparkSqlParser.TRUE || _la === SparkSqlParser.EQ || ((((_la - 279)) & ~0x1F) === 0 && ((1 << (_la - 279)) & ((1 << (SparkSqlParser.STRING - 279)) | (1 << (SparkSqlParser.INTEGER_VALUE - 279)) | (1 << (SparkSqlParser.DECIMAL_VALUE - 279)))) !== 0)) {
				{
				this.state = 1430;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.EQ) {
					{
					this.state = 1429;
					this.match(SparkSqlParser.EQ);
					}
				}

				this.state = 1432;
				_localctx._value = this.tablePropertyValue();
				}
			}

			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public tablePropertyKey(): TablePropertyKeyContext {
		let _localctx: TablePropertyKeyContext = new TablePropertyKeyContext(this._ctx, this.state);
		this.enterRule(_localctx, 62, SparkSqlParser.RULE_tablePropertyKey);
		let _la: number;
		try {
			this.state = 1444;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 156, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1435;
				this.identifier();
				this.state = 1440;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === SparkSqlParser.T__3) {
					{
					{
					this.state = 1436;
					this.match(SparkSqlParser.T__3);
					this.state = 1437;
					this.identifier();
					}
					}
					this.state = 1442;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1443;
				this.match(SparkSqlParser.STRING);
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public tablePropertyValue(): TablePropertyValueContext {
		let _localctx: TablePropertyValueContext = new TablePropertyValueContext(this._ctx, this.state);
		this.enterRule(_localctx, 64, SparkSqlParser.RULE_tablePropertyValue);
		try {
			this.state = 1450;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case SparkSqlParser.INTEGER_VALUE:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1446;
				this.match(SparkSqlParser.INTEGER_VALUE);
				}
				break;
			case SparkSqlParser.DECIMAL_VALUE:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1447;
				this.match(SparkSqlParser.DECIMAL_VALUE);
				}
				break;
			case SparkSqlParser.FALSE:
			case SparkSqlParser.TRUE:
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 1448;
				this.booleanValue();
				}
				break;
			case SparkSqlParser.STRING:
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 1449;
				this.match(SparkSqlParser.STRING);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public constantList(): ConstantListContext {
		let _localctx: ConstantListContext = new ConstantListContext(this._ctx, this.state);
		this.enterRule(_localctx, 66, SparkSqlParser.RULE_constantList);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1452;
			this.match(SparkSqlParser.T__0);
			this.state = 1453;
			this.constant();
			this.state = 1458;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__2) {
				{
				{
				this.state = 1454;
				this.match(SparkSqlParser.T__2);
				this.state = 1455;
				this.constant();
				}
				}
				this.state = 1460;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 1461;
			this.match(SparkSqlParser.T__1);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public nestedConstantList(): NestedConstantListContext {
		let _localctx: NestedConstantListContext = new NestedConstantListContext(this._ctx, this.state);
		this.enterRule(_localctx, 68, SparkSqlParser.RULE_nestedConstantList);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1463;
			this.match(SparkSqlParser.T__0);
			this.state = 1464;
			this.constantList();
			this.state = 1469;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__2) {
				{
				{
				this.state = 1465;
				this.match(SparkSqlParser.T__2);
				this.state = 1466;
				this.constantList();
				}
				}
				this.state = 1471;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 1472;
			this.match(SparkSqlParser.T__1);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public createFileFormat(): CreateFileFormatContext {
		let _localctx: CreateFileFormatContext = new CreateFileFormatContext(this._ctx, this.state);
		this.enterRule(_localctx, 70, SparkSqlParser.RULE_createFileFormat);
		try {
			this.state = 1480;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 160, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1474;
				this.match(SparkSqlParser.STORED);
				this.state = 1475;
				this.match(SparkSqlParser.AS);
				this.state = 1476;
				this.fileFormat();
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1477;
				this.match(SparkSqlParser.STORED);
				this.state = 1478;
				this.match(SparkSqlParser.BY);
				this.state = 1479;
				this.storageHandler();
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public fileFormat(): FileFormatContext {
		let _localctx: FileFormatContext = new FileFormatContext(this._ctx, this.state);
		this.enterRule(_localctx, 72, SparkSqlParser.RULE_fileFormat);
		try {
			this.state = 1487;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 161, this._ctx) ) {
			case 1:
				_localctx = new TableFileFormatContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1482;
				this.match(SparkSqlParser.INPUTFORMAT);
				this.state = 1483;
				(_localctx as TableFileFormatContext)._inFmt = this.match(SparkSqlParser.STRING);
				this.state = 1484;
				this.match(SparkSqlParser.OUTPUTFORMAT);
				this.state = 1485;
				(_localctx as TableFileFormatContext)._outFmt = this.match(SparkSqlParser.STRING);
				}
				break;

			case 2:
				_localctx = new GenericFileFormatContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1486;
				this.identifier();
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public storageHandler(): StorageHandlerContext {
		let _localctx: StorageHandlerContext = new StorageHandlerContext(this._ctx, this.state);
		this.enterRule(_localctx, 74, SparkSqlParser.RULE_storageHandler);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1489;
			this.match(SparkSqlParser.STRING);
			this.state = 1493;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 162, this._ctx) ) {
			case 1:
				{
				this.state = 1490;
				this.match(SparkSqlParser.WITH);
				this.state = 1491;
				this.match(SparkSqlParser.SERDEPROPERTIES);
				this.state = 1492;
				this.tablePropertyList();
				}
				break;
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public resource(): ResourceContext {
		let _localctx: ResourceContext = new ResourceContext(this._ctx, this.state);
		this.enterRule(_localctx, 76, SparkSqlParser.RULE_resource);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1495;
			this.identifier();
			this.state = 1496;
			this.match(SparkSqlParser.STRING);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public dmlStatementNoWith(): DmlStatementNoWithContext {
		let _localctx: DmlStatementNoWithContext = new DmlStatementNoWithContext(this._ctx, this.state);
		this.enterRule(_localctx, 78, SparkSqlParser.RULE_dmlStatementNoWith);
		let _la: number;
		try {
			let _alt: number;
			this.state = 1549;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case SparkSqlParser.INSERT:
				_localctx = new SingleInsertQueryContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1498;
				this.insertInto();
				this.state = 1499;
				this.queryTerm(0);
				this.state = 1500;
				this.queryOrganization();
				}
				break;
			case SparkSqlParser.FROM:
				_localctx = new MultiInsertQueryContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1502;
				this.fromClause();
				this.state = 1504;
				this._errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						this.state = 1503;
						this.multiInsertQueryBody();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					this.state = 1506;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 163, this._ctx);
				} while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER);
				}
				break;
			case SparkSqlParser.DELETE:
				_localctx = new DeleteFromTableContext(_localctx);
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 1508;
				this.match(SparkSqlParser.DELETE);
				this.state = 1509;
				this.match(SparkSqlParser.FROM);
				this.state = 1510;
				this.multipartIdentifier();
				this.state = 1511;
				this.tableAlias();
				this.state = 1513;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.WHERE) {
					{
					this.state = 1512;
					this.whereClause();
					}
				}

				}
				break;
			case SparkSqlParser.UPDATE:
				_localctx = new UpdateTableContext(_localctx);
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 1515;
				this.match(SparkSqlParser.UPDATE);
				this.state = 1516;
				this.multipartIdentifier();
				this.state = 1517;
				this.tableAlias();
				this.state = 1518;
				this.setClause();
				this.state = 1520;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.WHERE) {
					{
					this.state = 1519;
					this.whereClause();
					}
				}

				}
				break;
			case SparkSqlParser.MERGE:
				_localctx = new MergeIntoTableContext(_localctx);
				this.enterOuterAlt(_localctx, 5);
				{
				this.state = 1522;
				this.match(SparkSqlParser.MERGE);
				this.state = 1523;
				this.match(SparkSqlParser.INTO);
				this.state = 1524;
				(_localctx as MergeIntoTableContext)._target = this.multipartIdentifier();
				this.state = 1525;
				(_localctx as MergeIntoTableContext)._targetAlias = this.tableAlias();
				this.state = 1526;
				this.match(SparkSqlParser.USING);
				this.state = 1532;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 166, this._ctx) ) {
				case 1:
					{
					this.state = 1527;
					(_localctx as MergeIntoTableContext)._source = this.multipartIdentifier();
					}
					break;

				case 2:
					{
					this.state = 1528;
					this.match(SparkSqlParser.T__0);
					this.state = 1529;
					(_localctx as MergeIntoTableContext)._sourceQuery = this.query();
					this.state = 1530;
					this.match(SparkSqlParser.T__1);
					}
					break;
				}
				this.state = 1534;
				(_localctx as MergeIntoTableContext)._sourceAlias = this.tableAlias();
				this.state = 1535;
				this.match(SparkSqlParser.ON);
				this.state = 1536;
				(_localctx as MergeIntoTableContext)._mergeCondition = this.booleanExpression(0);
				this.state = 1540;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 167, this._ctx);
				while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1) {
						{
						{
						this.state = 1537;
						this.matchedClause();
						}
						}
					}
					this.state = 1542;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 167, this._ctx);
				}
				this.state = 1546;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === SparkSqlParser.WHEN) {
					{
					{
					this.state = 1543;
					this.notMatchedClause();
					}
					}
					this.state = 1548;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public queryOrganization(): QueryOrganizationContext {
		let _localctx: QueryOrganizationContext = new QueryOrganizationContext(this._ctx, this.state);
		this.enterRule(_localctx, 80, SparkSqlParser.RULE_queryOrganization);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1561;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 171, this._ctx) ) {
			case 1:
				{
				this.state = 1551;
				this.match(SparkSqlParser.ORDER);
				this.state = 1552;
				this.match(SparkSqlParser.BY);
				this.state = 1553;
				_localctx._sortItem = this.sortItem();
				_localctx._order.push(_localctx._sortItem);
				this.state = 1558;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 170, this._ctx);
				while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1) {
						{
						{
						this.state = 1554;
						this.match(SparkSqlParser.T__2);
						this.state = 1555;
						_localctx._sortItem = this.sortItem();
						_localctx._order.push(_localctx._sortItem);
						}
						}
					}
					this.state = 1560;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 170, this._ctx);
				}
				}
				break;
			}
			this.state = 1573;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 173, this._ctx) ) {
			case 1:
				{
				this.state = 1563;
				this.match(SparkSqlParser.CLUSTER);
				this.state = 1564;
				this.match(SparkSqlParser.BY);
				this.state = 1565;
				_localctx._expression = this.expression();
				_localctx._clusterBy.push(_localctx._expression);
				this.state = 1570;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 172, this._ctx);
				while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1) {
						{
						{
						this.state = 1566;
						this.match(SparkSqlParser.T__2);
						this.state = 1567;
						_localctx._expression = this.expression();
						_localctx._clusterBy.push(_localctx._expression);
						}
						}
					}
					this.state = 1572;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 172, this._ctx);
				}
				}
				break;
			}
			this.state = 1585;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 175, this._ctx) ) {
			case 1:
				{
				this.state = 1575;
				this.match(SparkSqlParser.DISTRIBUTE);
				this.state = 1576;
				this.match(SparkSqlParser.BY);
				this.state = 1577;
				_localctx._expression = this.expression();
				_localctx._distributeBy.push(_localctx._expression);
				this.state = 1582;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 174, this._ctx);
				while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1) {
						{
						{
						this.state = 1578;
						this.match(SparkSqlParser.T__2);
						this.state = 1579;
						_localctx._expression = this.expression();
						_localctx._distributeBy.push(_localctx._expression);
						}
						}
					}
					this.state = 1584;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 174, this._ctx);
				}
				}
				break;
			}
			this.state = 1597;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 177, this._ctx) ) {
			case 1:
				{
				this.state = 1587;
				this.match(SparkSqlParser.SORT);
				this.state = 1588;
				this.match(SparkSqlParser.BY);
				this.state = 1589;
				_localctx._sortItem = this.sortItem();
				_localctx._sort.push(_localctx._sortItem);
				this.state = 1594;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 176, this._ctx);
				while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1) {
						{
						{
						this.state = 1590;
						this.match(SparkSqlParser.T__2);
						this.state = 1591;
						_localctx._sortItem = this.sortItem();
						_localctx._sort.push(_localctx._sortItem);
						}
						}
					}
					this.state = 1596;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 176, this._ctx);
				}
				}
				break;
			}
			this.state = 1600;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 178, this._ctx) ) {
			case 1:
				{
				this.state = 1599;
				this.windowClause();
				}
				break;
			}
			this.state = 1607;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 180, this._ctx) ) {
			case 1:
				{
				this.state = 1602;
				this.match(SparkSqlParser.LIMIT);
				this.state = 1605;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 179, this._ctx) ) {
				case 1:
					{
					this.state = 1603;
					this.match(SparkSqlParser.ALL);
					}
					break;

				case 2:
					{
					this.state = 1604;
					_localctx._limit = this.expression();
					}
					break;
				}
				}
				break;
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public multiInsertQueryBody(): MultiInsertQueryBodyContext {
		let _localctx: MultiInsertQueryBodyContext = new MultiInsertQueryBodyContext(this._ctx, this.state);
		this.enterRule(_localctx, 82, SparkSqlParser.RULE_multiInsertQueryBody);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1609;
			this.insertInto();
			this.state = 1610;
			this.fromStatementBody();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}

	public queryTerm(): QueryTermContext;
	public queryTerm(_p: number): QueryTermContext;
	// @RuleVersion(0)
	public queryTerm(_p?: number): QueryTermContext {
		if (_p === undefined) {
			_p = 0;
		}

		let _parentctx: ParserRuleContext = this._ctx;
		let _parentState: number = this.state;
		let _localctx: QueryTermContext = new QueryTermContext(this._ctx, _parentState);
		let _prevctx: QueryTermContext = _localctx;
		let _startState: number = 84;
		this.enterRecursionRule(_localctx, 84, SparkSqlParser.RULE_queryTerm, _p);
		let _la: number;
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new QueryTermDefaultContext(_localctx);
			this._ctx = _localctx;
			_prevctx = _localctx;

			this.state = 1613;
			this.queryPrimary();
			}
			this._ctx._stop = this._input.tryLT(-1);
			this.state = 1638;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 185, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					if (this._parseListeners != null) {
						this.triggerExitRuleEvent();
					}
					_prevctx = _localctx;
					{
					this.state = 1636;
					this._errHandler.sync(this);
					switch ( this.interpreter.adaptivePredict(this._input, 184, this._ctx) ) {
					case 1:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						(_localctx as SetOperationContext)._left = _prevctx;
						this.pushNewRecursionContext(_localctx, _startState, SparkSqlParser.RULE_queryTerm);
						this.state = 1615;
						if (!(this.precpred(this._ctx, 3))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 3)");
						}
						this.state = 1616;
						if (!(legacy_setops_precedence_enbled)) {
							throw this.createFailedPredicateException("legacy_setops_precedence_enbled");
						}
						this.state = 1617;
						(_localctx as SetOperationContext)._operator = this._input.LT(1);
						_la = this._input.LA(1);
						if (!(_la === SparkSqlParser.EXCEPT || _la === SparkSqlParser.INTERSECT || _la === SparkSqlParser.SETMINUS || _la === SparkSqlParser.UNION)) {
							(_localctx as SetOperationContext)._operator = this._errHandler.recoverInline(this);
						} else {
							if (this._input.LA(1) === Token.EOF) {
								this.matchedEOF = true;
							}

							this._errHandler.reportMatch(this);
							this.consume();
						}
						this.state = 1619;
						this._errHandler.sync(this);
						_la = this._input.LA(1);
						if (_la === SparkSqlParser.ALL || _la === SparkSqlParser.DISTINCT) {
							{
							this.state = 1618;
							this.setQuantifier();
							}
						}

						this.state = 1621;
						(_localctx as SetOperationContext)._right = this.queryTerm(4);
						}
						break;

					case 2:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						(_localctx as SetOperationContext)._left = _prevctx;
						this.pushNewRecursionContext(_localctx, _startState, SparkSqlParser.RULE_queryTerm);
						this.state = 1622;
						if (!(this.precpred(this._ctx, 2))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 2)");
						}
						this.state = 1623;
						if (!(!legacy_setops_precedence_enbled)) {
							throw this.createFailedPredicateException("!legacy_setops_precedence_enbled");
						}
						this.state = 1624;
						(_localctx as SetOperationContext)._operator = this.match(SparkSqlParser.INTERSECT);
						this.state = 1626;
						this._errHandler.sync(this);
						_la = this._input.LA(1);
						if (_la === SparkSqlParser.ALL || _la === SparkSqlParser.DISTINCT) {
							{
							this.state = 1625;
							this.setQuantifier();
							}
						}

						this.state = 1628;
						(_localctx as SetOperationContext)._right = this.queryTerm(3);
						}
						break;

					case 3:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						(_localctx as SetOperationContext)._left = _prevctx;
						this.pushNewRecursionContext(_localctx, _startState, SparkSqlParser.RULE_queryTerm);
						this.state = 1629;
						if (!(this.precpred(this._ctx, 1))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 1)");
						}
						this.state = 1630;
						if (!(!legacy_setops_precedence_enbled)) {
							throw this.createFailedPredicateException("!legacy_setops_precedence_enbled");
						}
						this.state = 1631;
						(_localctx as SetOperationContext)._operator = this._input.LT(1);
						_la = this._input.LA(1);
						if (!(_la === SparkSqlParser.EXCEPT || _la === SparkSqlParser.SETMINUS || _la === SparkSqlParser.UNION)) {
							(_localctx as SetOperationContext)._operator = this._errHandler.recoverInline(this);
						} else {
							if (this._input.LA(1) === Token.EOF) {
								this.matchedEOF = true;
							}

							this._errHandler.reportMatch(this);
							this.consume();
						}
						this.state = 1633;
						this._errHandler.sync(this);
						_la = this._input.LA(1);
						if (_la === SparkSqlParser.ALL || _la === SparkSqlParser.DISTINCT) {
							{
							this.state = 1632;
							this.setQuantifier();
							}
						}

						this.state = 1635;
						(_localctx as SetOperationContext)._right = this.queryTerm(2);
						}
						break;
					}
					}
				}
				this.state = 1640;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 185, this._ctx);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public queryPrimary(): QueryPrimaryContext {
		let _localctx: QueryPrimaryContext = new QueryPrimaryContext(this._ctx, this.state);
		this.enterRule(_localctx, 86, SparkSqlParser.RULE_queryPrimary);
		try {
			this.state = 1650;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case SparkSqlParser.MAP:
			case SparkSqlParser.REDUCE:
			case SparkSqlParser.SELECT:
				_localctx = new QueryPrimaryDefaultContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1641;
				this.querySpecification();
				}
				break;
			case SparkSqlParser.FROM:
				_localctx = new FromStmtContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1642;
				this.fromStatement();
				}
				break;
			case SparkSqlParser.TABLE:
				_localctx = new TableContext(_localctx);
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 1643;
				this.match(SparkSqlParser.TABLE);
				this.state = 1644;
				this.multipartIdentifier();
				}
				break;
			case SparkSqlParser.VALUES:
				_localctx = new InlineTableDefault1Context(_localctx);
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 1645;
				this.inlineTable();
				}
				break;
			case SparkSqlParser.T__0:
				_localctx = new SubqueryContext(_localctx);
				this.enterOuterAlt(_localctx, 5);
				{
				this.state = 1646;
				this.match(SparkSqlParser.T__0);
				this.state = 1647;
				this.query();
				this.state = 1648;
				this.match(SparkSqlParser.T__1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public sortItem(): SortItemContext {
		let _localctx: SortItemContext = new SortItemContext(this._ctx, this.state);
		this.enterRule(_localctx, 88, SparkSqlParser.RULE_sortItem);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1652;
			this.expression();
			this.state = 1654;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 187, this._ctx) ) {
			case 1:
				{
				this.state = 1653;
				_localctx._ordering = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.ASC || _la === SparkSqlParser.DESC)) {
					_localctx._ordering = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
				break;
			}
			this.state = 1658;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 188, this._ctx) ) {
			case 1:
				{
				this.state = 1656;
				this.match(SparkSqlParser.NULLS);
				this.state = 1657;
				_localctx._nullOrder = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.FIRST || _la === SparkSqlParser.LAST)) {
					_localctx._nullOrder = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
				break;
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public fromStatement(): FromStatementContext {
		let _localctx: FromStatementContext = new FromStatementContext(this._ctx, this.state);
		this.enterRule(_localctx, 90, SparkSqlParser.RULE_fromStatement);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1660;
			this.fromClause();
			this.state = 1662;
			this._errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					this.state = 1661;
					this.fromStatementBody();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				this.state = 1664;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 189, this._ctx);
			} while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public fromStatementBody(): FromStatementBodyContext {
		let _localctx: FromStatementBodyContext = new FromStatementBodyContext(this._ctx, this.state);
		this.enterRule(_localctx, 92, SparkSqlParser.RULE_fromStatementBody);
		try {
			let _alt: number;
			this.state = 1693;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 196, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1666;
				this.transformClause();
				this.state = 1668;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 190, this._ctx) ) {
				case 1:
					{
					this.state = 1667;
					this.whereClause();
					}
					break;
				}
				this.state = 1670;
				this.queryOrganization();
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1672;
				this.selectClause();
				this.state = 1676;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 191, this._ctx);
				while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1) {
						{
						{
						this.state = 1673;
						this.lateralView();
						}
						}
					}
					this.state = 1678;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 191, this._ctx);
				}
				this.state = 1680;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 192, this._ctx) ) {
				case 1:
					{
					this.state = 1679;
					this.whereClause();
					}
					break;
				}
				this.state = 1683;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 193, this._ctx) ) {
				case 1:
					{
					this.state = 1682;
					this.aggregationClause();
					}
					break;
				}
				this.state = 1686;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 194, this._ctx) ) {
				case 1:
					{
					this.state = 1685;
					this.havingClause();
					}
					break;
				}
				this.state = 1689;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 195, this._ctx) ) {
				case 1:
					{
					this.state = 1688;
					this.windowClause();
					}
					break;
				}
				this.state = 1691;
				this.queryOrganization();
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public querySpecification(): QuerySpecificationContext {
		let _localctx: QuerySpecificationContext = new QuerySpecificationContext(this._ctx, this.state);
		this.enterRule(_localctx, 94, SparkSqlParser.RULE_querySpecification);
		try {
			let _alt: number;
			this.state = 1724;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 205, this._ctx) ) {
			case 1:
				_localctx = new TransformQuerySpecificationContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1695;
				this.transformClause();
				this.state = 1697;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 197, this._ctx) ) {
				case 1:
					{
					this.state = 1696;
					this.fromClause();
					}
					break;
				}
				this.state = 1700;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 198, this._ctx) ) {
				case 1:
					{
					this.state = 1699;
					this.whereClause();
					}
					break;
				}
				}
				break;

			case 2:
				_localctx = new RegularQuerySpecificationContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1702;
				this.selectClause();
				this.state = 1704;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 199, this._ctx) ) {
				case 1:
					{
					this.state = 1703;
					this.fromClause();
					}
					break;
				}
				this.state = 1709;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 200, this._ctx);
				while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1) {
						{
						{
						this.state = 1706;
						this.lateralView();
						}
						}
					}
					this.state = 1711;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 200, this._ctx);
				}
				this.state = 1713;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 201, this._ctx) ) {
				case 1:
					{
					this.state = 1712;
					this.whereClause();
					}
					break;
				}
				this.state = 1716;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 202, this._ctx) ) {
				case 1:
					{
					this.state = 1715;
					this.aggregationClause();
					}
					break;
				}
				this.state = 1719;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 203, this._ctx) ) {
				case 1:
					{
					this.state = 1718;
					this.havingClause();
					}
					break;
				}
				this.state = 1722;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 204, this._ctx) ) {
				case 1:
					{
					this.state = 1721;
					this.windowClause();
					}
					break;
				}
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public transformClause(): TransformClauseContext {
		let _localctx: TransformClauseContext = new TransformClauseContext(this._ctx, this.state);
		this.enterRule(_localctx, 96, SparkSqlParser.RULE_transformClause);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1736;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case SparkSqlParser.SELECT:
				{
				this.state = 1726;
				this.match(SparkSqlParser.SELECT);
				this.state = 1727;
				_localctx._kind = this.match(SparkSqlParser.TRANSFORM);
				this.state = 1728;
				this.match(SparkSqlParser.T__0);
				this.state = 1729;
				this.namedExpressionSeq();
				this.state = 1730;
				this.match(SparkSqlParser.T__1);
				}
				break;
			case SparkSqlParser.MAP:
				{
				this.state = 1732;
				_localctx._kind = this.match(SparkSqlParser.MAP);
				this.state = 1733;
				this.namedExpressionSeq();
				}
				break;
			case SparkSqlParser.REDUCE:
				{
				this.state = 1734;
				_localctx._kind = this.match(SparkSqlParser.REDUCE);
				this.state = 1735;
				this.namedExpressionSeq();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			this.state = 1739;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.ROW) {
				{
				this.state = 1738;
				_localctx._inRowFormat = this.rowFormat();
				}
			}

			this.state = 1743;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.RECORDWRITER) {
				{
				this.state = 1741;
				this.match(SparkSqlParser.RECORDWRITER);
				this.state = 1742;
				_localctx._recordWriter = this.match(SparkSqlParser.STRING);
				}
			}

			this.state = 1745;
			this.match(SparkSqlParser.USING);
			this.state = 1746;
			_localctx._script = this.match(SparkSqlParser.STRING);
			this.state = 1759;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 211, this._ctx) ) {
			case 1:
				{
				this.state = 1747;
				this.match(SparkSqlParser.AS);
				this.state = 1757;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 210, this._ctx) ) {
				case 1:
					{
					this.state = 1748;
					this.identifierSeq();
					}
					break;

				case 2:
					{
					this.state = 1749;
					this.colTypeList();
					}
					break;

				case 3:
					{
					{
					this.state = 1750;
					this.match(SparkSqlParser.T__0);
					this.state = 1753;
					this._errHandler.sync(this);
					switch ( this.interpreter.adaptivePredict(this._input, 209, this._ctx) ) {
					case 1:
						{
						this.state = 1751;
						this.identifierSeq();
						}
						break;

					case 2:
						{
						this.state = 1752;
						this.colTypeList();
						}
						break;
					}
					this.state = 1755;
					this.match(SparkSqlParser.T__1);
					}
					}
					break;
				}
				}
				break;
			}
			this.state = 1762;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 212, this._ctx) ) {
			case 1:
				{
				this.state = 1761;
				_localctx._outRowFormat = this.rowFormat();
				}
				break;
			}
			this.state = 1766;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 213, this._ctx) ) {
			case 1:
				{
				this.state = 1764;
				this.match(SparkSqlParser.RECORDREADER);
				this.state = 1765;
				_localctx._recordReader = this.match(SparkSqlParser.STRING);
				}
				break;
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public selectClause(): SelectClauseContext {
		let _localctx: SelectClauseContext = new SelectClauseContext(this._ctx, this.state);
		this.enterRule(_localctx, 98, SparkSqlParser.RULE_selectClause);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1768;
			this.match(SparkSqlParser.SELECT);
			this.state = 1772;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 214, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					{
					{
					this.state = 1769;
					_localctx._hint = this.hint();
					_localctx._hints.push(_localctx._hint);
					}
					}
				}
				this.state = 1774;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 214, this._ctx);
			}
			this.state = 1776;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 215, this._ctx) ) {
			case 1:
				{
				this.state = 1775;
				this.setQuantifier();
				}
				break;
			}
			this.state = 1778;
			this.namedExpressionSeq();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public setClause(): SetClauseContext {
		let _localctx: SetClauseContext = new SetClauseContext(this._ctx, this.state);
		this.enterRule(_localctx, 100, SparkSqlParser.RULE_setClause);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1780;
			this.match(SparkSqlParser.SET);
			this.state = 1781;
			this.assignmentList();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public matchedClause(): MatchedClauseContext {
		let _localctx: MatchedClauseContext = new MatchedClauseContext(this._ctx, this.state);
		this.enterRule(_localctx, 102, SparkSqlParser.RULE_matchedClause);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1783;
			this.match(SparkSqlParser.WHEN);
			this.state = 1784;
			this.match(SparkSqlParser.MATCHED);
			this.state = 1787;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.AND) {
				{
				this.state = 1785;
				this.match(SparkSqlParser.AND);
				this.state = 1786;
				_localctx._matchedCond = this.booleanExpression(0);
				}
			}

			this.state = 1789;
			this.match(SparkSqlParser.THEN);
			this.state = 1790;
			this.matchedAction();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public notMatchedClause(): NotMatchedClauseContext {
		let _localctx: NotMatchedClauseContext = new NotMatchedClauseContext(this._ctx, this.state);
		this.enterRule(_localctx, 104, SparkSqlParser.RULE_notMatchedClause);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1792;
			this.match(SparkSqlParser.WHEN);
			this.state = 1793;
			this.match(SparkSqlParser.NOT);
			this.state = 1794;
			this.match(SparkSqlParser.MATCHED);
			this.state = 1797;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.AND) {
				{
				this.state = 1795;
				this.match(SparkSqlParser.AND);
				this.state = 1796;
				_localctx._notMatchedCond = this.booleanExpression(0);
				}
			}

			this.state = 1799;
			this.match(SparkSqlParser.THEN);
			this.state = 1800;
			this.notMatchedAction();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public matchedAction(): MatchedActionContext {
		let _localctx: MatchedActionContext = new MatchedActionContext(this._ctx, this.state);
		this.enterRule(_localctx, 106, SparkSqlParser.RULE_matchedAction);
		try {
			this.state = 1809;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 218, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1802;
				this.match(SparkSqlParser.DELETE);
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1803;
				this.match(SparkSqlParser.UPDATE);
				this.state = 1804;
				this.match(SparkSqlParser.SET);
				this.state = 1805;
				this.match(SparkSqlParser.ASTERISK);
				}
				break;

			case 3:
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 1806;
				this.match(SparkSqlParser.UPDATE);
				this.state = 1807;
				this.match(SparkSqlParser.SET);
				this.state = 1808;
				this.assignmentList();
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public notMatchedAction(): NotMatchedActionContext {
		let _localctx: NotMatchedActionContext = new NotMatchedActionContext(this._ctx, this.state);
		this.enterRule(_localctx, 108, SparkSqlParser.RULE_notMatchedAction);
		let _la: number;
		try {
			this.state = 1829;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 220, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1811;
				this.match(SparkSqlParser.INSERT);
				this.state = 1812;
				this.match(SparkSqlParser.ASTERISK);
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1813;
				this.match(SparkSqlParser.INSERT);
				this.state = 1814;
				this.match(SparkSqlParser.T__0);
				this.state = 1815;
				_localctx._columns = this.multipartIdentifierList();
				this.state = 1816;
				this.match(SparkSqlParser.T__1);
				this.state = 1817;
				this.match(SparkSqlParser.VALUES);
				this.state = 1818;
				this.match(SparkSqlParser.T__0);
				this.state = 1819;
				this.expression();
				this.state = 1824;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === SparkSqlParser.T__2) {
					{
					{
					this.state = 1820;
					this.match(SparkSqlParser.T__2);
					this.state = 1821;
					this.expression();
					}
					}
					this.state = 1826;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				this.state = 1827;
				this.match(SparkSqlParser.T__1);
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public assignmentList(): AssignmentListContext {
		let _localctx: AssignmentListContext = new AssignmentListContext(this._ctx, this.state);
		this.enterRule(_localctx, 110, SparkSqlParser.RULE_assignmentList);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1831;
			this.assignment();
			this.state = 1836;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__2) {
				{
				{
				this.state = 1832;
				this.match(SparkSqlParser.T__2);
				this.state = 1833;
				this.assignment();
				}
				}
				this.state = 1838;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public assignment(): AssignmentContext {
		let _localctx: AssignmentContext = new AssignmentContext(this._ctx, this.state);
		this.enterRule(_localctx, 112, SparkSqlParser.RULE_assignment);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1839;
			_localctx._key = this.multipartIdentifier();
			this.state = 1840;
			this.match(SparkSqlParser.EQ);
			this.state = 1841;
			_localctx._value = this.expression();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public whereClause(): WhereClauseContext {
		let _localctx: WhereClauseContext = new WhereClauseContext(this._ctx, this.state);
		this.enterRule(_localctx, 114, SparkSqlParser.RULE_whereClause);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1843;
			this.match(SparkSqlParser.WHERE);
			this.state = 1844;
			this.booleanExpression(0);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public havingClause(): HavingClauseContext {
		let _localctx: HavingClauseContext = new HavingClauseContext(this._ctx, this.state);
		this.enterRule(_localctx, 116, SparkSqlParser.RULE_havingClause);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1846;
			this.match(SparkSqlParser.HAVING);
			this.state = 1847;
			this.booleanExpression(0);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public hint(): HintContext {
		let _localctx: HintContext = new HintContext(this._ctx, this.state);
		this.enterRule(_localctx, 118, SparkSqlParser.RULE_hint);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1849;
			this.match(SparkSqlParser.T__4);
			this.state = 1850;
			_localctx._hintStatement = this.hintStatement();
			_localctx._hintStatements.push(_localctx._hintStatement);
			this.state = 1857;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 223, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					{
					{
					this.state = 1852;
					this._errHandler.sync(this);
					switch ( this.interpreter.adaptivePredict(this._input, 222, this._ctx) ) {
					case 1:
						{
						this.state = 1851;
						this.match(SparkSqlParser.T__2);
						}
						break;
					}
					this.state = 1854;
					_localctx._hintStatement = this.hintStatement();
					_localctx._hintStatements.push(_localctx._hintStatement);
					}
					}
				}
				this.state = 1859;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 223, this._ctx);
			}
			this.state = 1860;
			this.match(SparkSqlParser.T__5);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public hintStatement(): HintStatementContext {
		let _localctx: HintStatementContext = new HintStatementContext(this._ctx, this.state);
		this.enterRule(_localctx, 120, SparkSqlParser.RULE_hintStatement);
		let _la: number;
		try {
			this.state = 1875;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 225, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1862;
				_localctx._hintName = this.identifier();
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1863;
				_localctx._hintName = this.identifier();
				this.state = 1864;
				this.match(SparkSqlParser.T__0);
				this.state = 1865;
				_localctx._primaryExpression = this.primaryExpression(0);
				_localctx._parameters.push(_localctx._primaryExpression);
				this.state = 1870;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === SparkSqlParser.T__2) {
					{
					{
					this.state = 1866;
					this.match(SparkSqlParser.T__2);
					this.state = 1867;
					_localctx._primaryExpression = this.primaryExpression(0);
					_localctx._parameters.push(_localctx._primaryExpression);
					}
					}
					this.state = 1872;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				this.state = 1873;
				this.match(SparkSqlParser.T__1);
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public fromClause(): FromClauseContext {
		let _localctx: FromClauseContext = new FromClauseContext(this._ctx, this.state);
		this.enterRule(_localctx, 122, SparkSqlParser.RULE_fromClause);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1877;
			this.match(SparkSqlParser.FROM);
			this.state = 1878;
			this.relation();
			this.state = 1879;
			this.suggestionRelation();
			this.state = 1886;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 226, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					{
					{
					this.state = 1880;
					this.match(SparkSqlParser.T__2);
					this.state = 1881;
					this.suggestionRelation();
					this.state = 1882;
					this.relation();
					}
					}
				}
				this.state = 1888;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 226, this._ctx);
			}
			this.state = 1892;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 227, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					{
					{
					this.state = 1889;
					this.lateralView();
					}
					}
				}
				this.state = 1894;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 227, this._ctx);
			}
			this.state = 1896;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 228, this._ctx) ) {
			case 1:
				{
				this.state = 1895;
				this.pivotClause();
				}
				break;
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public suggestionRelation(): SuggestionRelationContext {
		let _localctx: SuggestionRelationContext = new SuggestionRelationContext(this._ctx, this.state);
		this.enterRule(_localctx, 124, SparkSqlParser.RULE_suggestionRelation);
		try {
			this.enterOuterAlt(_localctx, 1);
			// tslint:disable-next-line:no-empty
			{
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public aggregationClause(): AggregationClauseContext {
		let _localctx: AggregationClauseContext = new AggregationClauseContext(this._ctx, this.state);
		this.enterRule(_localctx, 126, SparkSqlParser.RULE_aggregationClause);
		let _la: number;
		try {
			let _alt: number;
			this.state = 1944;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 233, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1900;
				this.match(SparkSqlParser.GROUP);
				this.state = 1901;
				this.match(SparkSqlParser.BY);
				this.state = 1902;
				_localctx._expression = this.expression();
				_localctx._groupingExpressions.push(_localctx._expression);
				this.state = 1907;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 229, this._ctx);
				while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1) {
						{
						{
						this.state = 1903;
						this.match(SparkSqlParser.T__2);
						this.state = 1904;
						_localctx._expression = this.expression();
						_localctx._groupingExpressions.push(_localctx._expression);
						}
						}
					}
					this.state = 1909;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 229, this._ctx);
				}
				this.state = 1927;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 231, this._ctx) ) {
				case 1:
					{
					this.state = 1910;
					this.match(SparkSqlParser.WITH);
					this.state = 1911;
					_localctx._kind = this.match(SparkSqlParser.ROLLUP);
					}
					break;

				case 2:
					{
					this.state = 1912;
					this.match(SparkSqlParser.WITH);
					this.state = 1913;
					_localctx._kind = this.match(SparkSqlParser.CUBE);
					}
					break;

				case 3:
					{
					this.state = 1914;
					_localctx._kind = this.match(SparkSqlParser.GROUPING);
					this.state = 1915;
					this.match(SparkSqlParser.SETS);
					this.state = 1916;
					this.match(SparkSqlParser.T__0);
					this.state = 1917;
					this.groupingSet();
					this.state = 1922;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					while (_la === SparkSqlParser.T__2) {
						{
						{
						this.state = 1918;
						this.match(SparkSqlParser.T__2);
						this.state = 1919;
						this.groupingSet();
						}
						}
						this.state = 1924;
						this._errHandler.sync(this);
						_la = this._input.LA(1);
					}
					this.state = 1925;
					this.match(SparkSqlParser.T__1);
					}
					break;
				}
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1929;
				this.match(SparkSqlParser.GROUP);
				this.state = 1930;
				this.match(SparkSqlParser.BY);
				this.state = 1931;
				_localctx._kind = this.match(SparkSqlParser.GROUPING);
				this.state = 1932;
				this.match(SparkSqlParser.SETS);
				this.state = 1933;
				this.match(SparkSqlParser.T__0);
				this.state = 1934;
				this.groupingSet();
				this.state = 1939;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === SparkSqlParser.T__2) {
					{
					{
					this.state = 1935;
					this.match(SparkSqlParser.T__2);
					this.state = 1936;
					this.groupingSet();
					}
					}
					this.state = 1941;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				this.state = 1942;
				this.match(SparkSqlParser.T__1);
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public groupingSet(): GroupingSetContext {
		let _localctx: GroupingSetContext = new GroupingSetContext(this._ctx, this.state);
		this.enterRule(_localctx, 128, SparkSqlParser.RULE_groupingSet);
		let _la: number;
		try {
			this.state = 1959;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 236, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1946;
				this.match(SparkSqlParser.T__0);
				this.state = 1955;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 235, this._ctx) ) {
				case 1:
					{
					this.state = 1947;
					this.expression();
					this.state = 1952;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					while (_la === SparkSqlParser.T__2) {
						{
						{
						this.state = 1948;
						this.match(SparkSqlParser.T__2);
						this.state = 1949;
						this.expression();
						}
						}
						this.state = 1954;
						this._errHandler.sync(this);
						_la = this._input.LA(1);
					}
					}
					break;
				}
				this.state = 1957;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1958;
				this.expression();
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public pivotClause(): PivotClauseContext {
		let _localctx: PivotClauseContext = new PivotClauseContext(this._ctx, this.state);
		this.enterRule(_localctx, 130, SparkSqlParser.RULE_pivotClause);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1961;
			this.match(SparkSqlParser.PIVOT);
			this.state = 1962;
			this.match(SparkSqlParser.T__0);
			this.state = 1963;
			_localctx._aggregates = this.namedExpressionSeq();
			this.state = 1964;
			this.match(SparkSqlParser.FOR);
			this.state = 1965;
			this.pivotColumn();
			this.state = 1966;
			this.match(SparkSqlParser.IN);
			this.state = 1967;
			this.match(SparkSqlParser.T__0);
			this.state = 1968;
			_localctx._pivotValue = this.pivotValue();
			_localctx._pivotValues.push(_localctx._pivotValue);
			this.state = 1973;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__2) {
				{
				{
				this.state = 1969;
				this.match(SparkSqlParser.T__2);
				this.state = 1970;
				_localctx._pivotValue = this.pivotValue();
				_localctx._pivotValues.push(_localctx._pivotValue);
				}
				}
				this.state = 1975;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 1976;
			this.match(SparkSqlParser.T__1);
			this.state = 1977;
			this.match(SparkSqlParser.T__1);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public pivotColumn(): PivotColumnContext {
		let _localctx: PivotColumnContext = new PivotColumnContext(this._ctx, this.state);
		this.enterRule(_localctx, 132, SparkSqlParser.RULE_pivotColumn);
		let _la: number;
		try {
			this.state = 1991;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 239, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 1979;
				_localctx._identifier = this.identifier();
				_localctx._identifiers.push(_localctx._identifier);
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 1980;
				this.match(SparkSqlParser.T__0);
				this.state = 1981;
				_localctx._identifier = this.identifier();
				_localctx._identifiers.push(_localctx._identifier);
				this.state = 1986;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === SparkSqlParser.T__2) {
					{
					{
					this.state = 1982;
					this.match(SparkSqlParser.T__2);
					this.state = 1983;
					_localctx._identifier = this.identifier();
					_localctx._identifiers.push(_localctx._identifier);
					}
					}
					this.state = 1988;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				this.state = 1989;
				this.match(SparkSqlParser.T__1);
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public pivotValue(): PivotValueContext {
		let _localctx: PivotValueContext = new PivotValueContext(this._ctx, this.state);
		this.enterRule(_localctx, 134, SparkSqlParser.RULE_pivotValue);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 1993;
			this.expression();
			this.state = 1998;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 241, this._ctx) ) {
			case 1:
				{
				this.state = 1995;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 240, this._ctx) ) {
				case 1:
					{
					this.state = 1994;
					this.match(SparkSqlParser.AS);
					}
					break;
				}
				this.state = 1997;
				this.identifier();
				}
				break;
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public lateralView(): LateralViewContext {
		let _localctx: LateralViewContext = new LateralViewContext(this._ctx, this.state);
		this.enterRule(_localctx, 136, SparkSqlParser.RULE_lateralView);
		let _la: number;
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2000;
			this.match(SparkSqlParser.LATERAL);
			this.state = 2001;
			this.match(SparkSqlParser.VIEW);
			this.state = 2003;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 242, this._ctx) ) {
			case 1:
				{
				this.state = 2002;
				this.match(SparkSqlParser.OUTER);
				}
				break;
			}
			this.state = 2005;
			this.qualifiedName();
			this.state = 2006;
			this.match(SparkSqlParser.T__0);
			this.state = 2015;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 244, this._ctx) ) {
			case 1:
				{
				this.state = 2007;
				this.expression();
				this.state = 2012;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === SparkSqlParser.T__2) {
					{
					{
					this.state = 2008;
					this.match(SparkSqlParser.T__2);
					this.state = 2009;
					this.expression();
					}
					}
					this.state = 2014;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				}
				break;
			}
			this.state = 2017;
			this.match(SparkSqlParser.T__1);
			this.state = 2018;
			_localctx._tblName = this.identifier();
			this.state = 2030;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 247, this._ctx) ) {
			case 1:
				{
				this.state = 2020;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 245, this._ctx) ) {
				case 1:
					{
					this.state = 2019;
					this.match(SparkSqlParser.AS);
					}
					break;
				}
				this.state = 2022;
				_localctx._identifier = this.identifier();
				_localctx._colName.push(_localctx._identifier);
				this.state = 2027;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 246, this._ctx);
				while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
					if (_alt === 1) {
						{
						{
						this.state = 2023;
						this.match(SparkSqlParser.T__2);
						this.state = 2024;
						_localctx._identifier = this.identifier();
						_localctx._colName.push(_localctx._identifier);
						}
						}
					}
					this.state = 2029;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 246, this._ctx);
				}
				}
				break;
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public setQuantifier(): SetQuantifierContext {
		let _localctx: SetQuantifierContext = new SetQuantifierContext(this._ctx, this.state);
		this.enterRule(_localctx, 138, SparkSqlParser.RULE_setQuantifier);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2032;
			_la = this._input.LA(1);
			if (!(_la === SparkSqlParser.ALL || _la === SparkSqlParser.DISTINCT)) {
			this._errHandler.recoverInline(this);
			} else {
				if (this._input.LA(1) === Token.EOF) {
					this.matchedEOF = true;
				}

				this._errHandler.reportMatch(this);
				this.consume();
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public relation(): RelationContext {
		let _localctx: RelationContext = new RelationContext(this._ctx, this.state);
		this.enterRule(_localctx, 140, SparkSqlParser.RULE_relation);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2034;
			this.relationPrimary();
			this.state = 2038;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 248, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					{
					{
					this.state = 2035;
					this.joinRelation();
					}
					}
				}
				this.state = 2040;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 248, this._ctx);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public joinRelation(): JoinRelationContext {
		let _localctx: JoinRelationContext = new JoinRelationContext(this._ctx, this.state);
		this.enterRule(_localctx, 142, SparkSqlParser.RULE_joinRelation);
		try {
			this.state = 2052;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case SparkSqlParser.ANTI:
			case SparkSqlParser.CROSS:
			case SparkSqlParser.FULL:
			case SparkSqlParser.INNER:
			case SparkSqlParser.JOIN:
			case SparkSqlParser.LEFT:
			case SparkSqlParser.RIGHT:
			case SparkSqlParser.SEMI:
				this.enterOuterAlt(_localctx, 1);
				{
				{
				this.state = 2041;
				this.joinType();
				}
				this.state = 2042;
				this.match(SparkSqlParser.JOIN);
				this.state = 2043;
				_localctx._right = this.relationPrimary();
				this.state = 2045;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 249, this._ctx) ) {
				case 1:
					{
					this.state = 2044;
					this.joinCriteria();
					}
					break;
				}
				}
				break;
			case SparkSqlParser.NATURAL:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2047;
				this.match(SparkSqlParser.NATURAL);
				this.state = 2048;
				this.joinType();
				this.state = 2049;
				this.match(SparkSqlParser.JOIN);
				this.state = 2050;
				_localctx._right = this.relationPrimary();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public joinType(): JoinTypeContext {
		let _localctx: JoinTypeContext = new JoinTypeContext(this._ctx, this.state);
		this.enterRule(_localctx, 144, SparkSqlParser.RULE_joinType);
		let _la: number;
		try {
			this.state = 2078;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 257, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2055;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.INNER) {
					{
					this.state = 2054;
					this.match(SparkSqlParser.INNER);
					}
				}

				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2057;
				this.match(SparkSqlParser.CROSS);
				}
				break;

			case 3:
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 2058;
				this.match(SparkSqlParser.LEFT);
				this.state = 2060;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.OUTER) {
					{
					this.state = 2059;
					this.match(SparkSqlParser.OUTER);
					}
				}

				}
				break;

			case 4:
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 2063;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.LEFT) {
					{
					this.state = 2062;
					this.match(SparkSqlParser.LEFT);
					}
				}

				this.state = 2065;
				this.match(SparkSqlParser.SEMI);
				}
				break;

			case 5:
				this.enterOuterAlt(_localctx, 5);
				{
				this.state = 2066;
				this.match(SparkSqlParser.RIGHT);
				this.state = 2068;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.OUTER) {
					{
					this.state = 2067;
					this.match(SparkSqlParser.OUTER);
					}
				}

				}
				break;

			case 6:
				this.enterOuterAlt(_localctx, 6);
				{
				this.state = 2070;
				this.match(SparkSqlParser.FULL);
				this.state = 2072;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.OUTER) {
					{
					this.state = 2071;
					this.match(SparkSqlParser.OUTER);
					}
				}

				}
				break;

			case 7:
				this.enterOuterAlt(_localctx, 7);
				{
				this.state = 2075;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.LEFT) {
					{
					this.state = 2074;
					this.match(SparkSqlParser.LEFT);
					}
				}

				this.state = 2077;
				this.match(SparkSqlParser.ANTI);
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public joinCriteria(): JoinCriteriaContext {
		let _localctx: JoinCriteriaContext = new JoinCriteriaContext(this._ctx, this.state);
		this.enterRule(_localctx, 146, SparkSqlParser.RULE_joinCriteria);
		try {
			this.state = 2084;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case SparkSqlParser.ON:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2080;
				this.match(SparkSqlParser.ON);
				this.state = 2081;
				this.booleanExpression(0);
				}
				break;
			case SparkSqlParser.USING:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2082;
				this.match(SparkSqlParser.USING);
				this.state = 2083;
				this.identifierList();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public sample(): SampleContext {
		let _localctx: SampleContext = new SampleContext(this._ctx, this.state);
		this.enterRule(_localctx, 148, SparkSqlParser.RULE_sample);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2086;
			this.match(SparkSqlParser.TABLESAMPLE);
			this.state = 2087;
			this.match(SparkSqlParser.T__0);
			this.state = 2089;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 259, this._ctx) ) {
			case 1:
				{
				this.state = 2088;
				this.sampleMethod();
				}
				break;
			}
			this.state = 2091;
			this.match(SparkSqlParser.T__1);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public sampleMethod(): SampleMethodContext {
		let _localctx: SampleMethodContext = new SampleMethodContext(this._ctx, this.state);
		this.enterRule(_localctx, 150, SparkSqlParser.RULE_sampleMethod);
		let _la: number;
		try {
			this.state = 2117;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 263, this._ctx) ) {
			case 1:
				_localctx = new SampleByPercentileContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2094;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.MINUS) {
					{
					this.state = 2093;
					(_localctx as SampleByPercentileContext)._negativeSign = this.match(SparkSqlParser.MINUS);
					}
				}

				this.state = 2096;
				(_localctx as SampleByPercentileContext)._percentage = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.INTEGER_VALUE || _la === SparkSqlParser.DECIMAL_VALUE)) {
					(_localctx as SampleByPercentileContext)._percentage = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 2097;
				this.match(SparkSqlParser.PERCENTLIT);
				}
				break;

			case 2:
				_localctx = new SampleByRowsContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2098;
				this.expression();
				this.state = 2099;
				this.match(SparkSqlParser.ROWS);
				}
				break;

			case 3:
				_localctx = new SampleByBucketContext(_localctx);
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 2101;
				(_localctx as SampleByBucketContext)._sampleType = this.match(SparkSqlParser.BUCKET);
				this.state = 2102;
				(_localctx as SampleByBucketContext)._numerator = this.match(SparkSqlParser.INTEGER_VALUE);
				this.state = 2103;
				this.match(SparkSqlParser.OUT);
				this.state = 2104;
				this.match(SparkSqlParser.OF);
				this.state = 2105;
				(_localctx as SampleByBucketContext)._denominator = this.match(SparkSqlParser.INTEGER_VALUE);
				this.state = 2114;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.ON) {
					{
					this.state = 2106;
					this.match(SparkSqlParser.ON);
					this.state = 2112;
					this._errHandler.sync(this);
					switch ( this.interpreter.adaptivePredict(this._input, 261, this._ctx) ) {
					case 1:
						{
						this.state = 2107;
						this.identifier();
						}
						break;

					case 2:
						{
						this.state = 2108;
						this.qualifiedName();
						this.state = 2109;
						this.match(SparkSqlParser.T__0);
						this.state = 2110;
						this.match(SparkSqlParser.T__1);
						}
						break;
					}
					}
				}

				}
				break;

			case 4:
				_localctx = new SampleByBytesContext(_localctx);
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 2116;
				(_localctx as SampleByBytesContext)._bytes = this.expression();
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public identifierList(): IdentifierListContext {
		let _localctx: IdentifierListContext = new IdentifierListContext(this._ctx, this.state);
		this.enterRule(_localctx, 152, SparkSqlParser.RULE_identifierList);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2119;
			this.match(SparkSqlParser.T__0);
			this.state = 2120;
			this.identifierSeq();
			this.state = 2121;
			this.match(SparkSqlParser.T__1);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public identifierSeq(): IdentifierSeqContext {
		let _localctx: IdentifierSeqContext = new IdentifierSeqContext(this._ctx, this.state);
		this.enterRule(_localctx, 154, SparkSqlParser.RULE_identifierSeq);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2123;
			_localctx._errorCapturingIdentifier = this.errorCapturingIdentifier();
			_localctx._ident.push(_localctx._errorCapturingIdentifier);
			this.state = 2128;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 264, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					{
					{
					this.state = 2124;
					this.match(SparkSqlParser.T__2);
					this.state = 2125;
					_localctx._errorCapturingIdentifier = this.errorCapturingIdentifier();
					_localctx._ident.push(_localctx._errorCapturingIdentifier);
					}
					}
				}
				this.state = 2130;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 264, this._ctx);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public orderedIdentifierList(): OrderedIdentifierListContext {
		let _localctx: OrderedIdentifierListContext = new OrderedIdentifierListContext(this._ctx, this.state);
		this.enterRule(_localctx, 156, SparkSqlParser.RULE_orderedIdentifierList);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2131;
			this.match(SparkSqlParser.T__0);
			this.state = 2132;
			this.orderedIdentifier();
			this.state = 2137;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__2) {
				{
				{
				this.state = 2133;
				this.match(SparkSqlParser.T__2);
				this.state = 2134;
				this.orderedIdentifier();
				}
				}
				this.state = 2139;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 2140;
			this.match(SparkSqlParser.T__1);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public orderedIdentifier(): OrderedIdentifierContext {
		let _localctx: OrderedIdentifierContext = new OrderedIdentifierContext(this._ctx, this.state);
		this.enterRule(_localctx, 158, SparkSqlParser.RULE_orderedIdentifier);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2142;
			_localctx._ident = this.errorCapturingIdentifier();
			this.state = 2144;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.ASC || _la === SparkSqlParser.DESC) {
				{
				this.state = 2143;
				_localctx._ordering = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.ASC || _la === SparkSqlParser.DESC)) {
					_localctx._ordering = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
			}

			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public identifierCommentList(): IdentifierCommentListContext {
		let _localctx: IdentifierCommentListContext = new IdentifierCommentListContext(this._ctx, this.state);
		this.enterRule(_localctx, 160, SparkSqlParser.RULE_identifierCommentList);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2146;
			this.match(SparkSqlParser.T__0);
			this.state = 2147;
			this.identifierComment();
			this.state = 2152;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__2) {
				{
				{
				this.state = 2148;
				this.match(SparkSqlParser.T__2);
				this.state = 2149;
				this.identifierComment();
				}
				}
				this.state = 2154;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 2155;
			this.match(SparkSqlParser.T__1);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public identifierComment(): IdentifierCommentContext {
		let _localctx: IdentifierCommentContext = new IdentifierCommentContext(this._ctx, this.state);
		this.enterRule(_localctx, 162, SparkSqlParser.RULE_identifierComment);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2157;
			this.identifier();
			this.state = 2159;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.COMMENT) {
				{
				this.state = 2158;
				this.commentSpec();
				}
			}

			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public relationPrimary(): RelationPrimaryContext {
		let _localctx: RelationPrimaryContext = new RelationPrimaryContext(this._ctx, this.state);
		this.enterRule(_localctx, 164, SparkSqlParser.RULE_relationPrimary);
		try {
			this.state = 2185;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 272, this._ctx) ) {
			case 1:
				_localctx = new TableNameContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2161;
				this.multipartIdentifier();
				this.state = 2163;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 269, this._ctx) ) {
				case 1:
					{
					this.state = 2162;
					this.sample();
					}
					break;
				}
				this.state = 2165;
				this.tableAlias();
				}
				break;

			case 2:
				_localctx = new AliasedQueryContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2167;
				this.match(SparkSqlParser.T__0);
				this.state = 2168;
				this.query();
				this.state = 2169;
				this.match(SparkSqlParser.T__1);
				this.state = 2171;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 270, this._ctx) ) {
				case 1:
					{
					this.state = 2170;
					this.sample();
					}
					break;
				}
				this.state = 2173;
				this.tableAlias();
				}
				break;

			case 3:
				_localctx = new AliasedRelationContext(_localctx);
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 2175;
				this.match(SparkSqlParser.T__0);
				this.state = 2176;
				this.relation();
				this.state = 2177;
				this.match(SparkSqlParser.T__1);
				this.state = 2179;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 271, this._ctx) ) {
				case 1:
					{
					this.state = 2178;
					this.sample();
					}
					break;
				}
				this.state = 2181;
				this.tableAlias();
				}
				break;

			case 4:
				_localctx = new InlineTableDefault2Context(_localctx);
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 2183;
				this.inlineTable();
				}
				break;

			case 5:
				_localctx = new TableValuedFunctionContext(_localctx);
				this.enterOuterAlt(_localctx, 5);
				{
				this.state = 2184;
				this.functionTable();
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public inlineTable(): InlineTableContext {
		let _localctx: InlineTableContext = new InlineTableContext(this._ctx, this.state);
		this.enterRule(_localctx, 166, SparkSqlParser.RULE_inlineTable);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2187;
			this.match(SparkSqlParser.VALUES);
			this.state = 2188;
			this.expression();
			this.state = 2193;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 273, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					{
					{
					this.state = 2189;
					this.match(SparkSqlParser.T__2);
					this.state = 2190;
					this.expression();
					}
					}
				}
				this.state = 2195;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 273, this._ctx);
			}
			this.state = 2196;
			this.tableAlias();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public functionTable(): FunctionTableContext {
		let _localctx: FunctionTableContext = new FunctionTableContext(this._ctx, this.state);
		this.enterRule(_localctx, 168, SparkSqlParser.RULE_functionTable);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2198;
			_localctx._funcName = this.errorCapturingIdentifier();
			this.state = 2199;
			this.match(SparkSqlParser.T__0);
			this.state = 2208;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 275, this._ctx) ) {
			case 1:
				{
				this.state = 2200;
				this.expression();
				this.state = 2205;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === SparkSqlParser.T__2) {
					{
					{
					this.state = 2201;
					this.match(SparkSqlParser.T__2);
					this.state = 2202;
					this.expression();
					}
					}
					this.state = 2207;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				}
				break;
			}
			this.state = 2210;
			this.match(SparkSqlParser.T__1);
			this.state = 2211;
			this.tableAlias();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public tableAlias(): TableAliasContext {
		let _localctx: TableAliasContext = new TableAliasContext(this._ctx, this.state);
		this.enterRule(_localctx, 170, SparkSqlParser.RULE_tableAlias);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2220;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 278, this._ctx) ) {
			case 1:
				{
				this.state = 2214;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 276, this._ctx) ) {
				case 1:
					{
					this.state = 2213;
					this.match(SparkSqlParser.AS);
					}
					break;
				}
				this.state = 2216;
				this.strictIdentifier();
				this.state = 2218;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 277, this._ctx) ) {
				case 1:
					{
					this.state = 2217;
					this.identifierList();
					}
					break;
				}
				}
				break;
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public rowFormat(): RowFormatContext {
		let _localctx: RowFormatContext = new RowFormatContext(this._ctx, this.state);
		this.enterRule(_localctx, 172, SparkSqlParser.RULE_rowFormat);
		try {
			this.state = 2271;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 286, this._ctx) ) {
			case 1:
				_localctx = new RowFormatSerdeContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2222;
				this.match(SparkSqlParser.ROW);
				this.state = 2223;
				this.match(SparkSqlParser.FORMAT);
				this.state = 2224;
				this.match(SparkSqlParser.SERDE);
				this.state = 2225;
				(_localctx as RowFormatSerdeContext)._name = this.match(SparkSqlParser.STRING);
				this.state = 2229;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 279, this._ctx) ) {
				case 1:
					{
					this.state = 2226;
					this.match(SparkSqlParser.WITH);
					this.state = 2227;
					this.match(SparkSqlParser.SERDEPROPERTIES);
					this.state = 2228;
					(_localctx as RowFormatSerdeContext)._props = this.tablePropertyList();
					}
					break;
				}
				}
				break;

			case 2:
				_localctx = new RowFormatDelimitedContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2231;
				this.match(SparkSqlParser.ROW);
				this.state = 2232;
				this.match(SparkSqlParser.FORMAT);
				this.state = 2233;
				this.match(SparkSqlParser.DELIMITED);
				this.state = 2243;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 281, this._ctx) ) {
				case 1:
					{
					this.state = 2234;
					this.match(SparkSqlParser.FIELDS);
					this.state = 2235;
					this.match(SparkSqlParser.TERMINATED);
					this.state = 2236;
					this.match(SparkSqlParser.BY);
					this.state = 2237;
					(_localctx as RowFormatDelimitedContext)._fieldsTerminatedBy = this.match(SparkSqlParser.STRING);
					this.state = 2241;
					this._errHandler.sync(this);
					switch ( this.interpreter.adaptivePredict(this._input, 280, this._ctx) ) {
					case 1:
						{
						this.state = 2238;
						this.match(SparkSqlParser.ESCAPED);
						this.state = 2239;
						this.match(SparkSqlParser.BY);
						this.state = 2240;
						(_localctx as RowFormatDelimitedContext)._escapedBy = this.match(SparkSqlParser.STRING);
						}
						break;
					}
					}
					break;
				}
				this.state = 2250;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 282, this._ctx) ) {
				case 1:
					{
					this.state = 2245;
					this.match(SparkSqlParser.COLLECTION);
					this.state = 2246;
					this.match(SparkSqlParser.ITEMS);
					this.state = 2247;
					this.match(SparkSqlParser.TERMINATED);
					this.state = 2248;
					this.match(SparkSqlParser.BY);
					this.state = 2249;
					(_localctx as RowFormatDelimitedContext)._collectionItemsTerminatedBy = this.match(SparkSqlParser.STRING);
					}
					break;
				}
				this.state = 2257;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 283, this._ctx) ) {
				case 1:
					{
					this.state = 2252;
					this.match(SparkSqlParser.MAP);
					this.state = 2253;
					this.match(SparkSqlParser.KEYS);
					this.state = 2254;
					this.match(SparkSqlParser.TERMINATED);
					this.state = 2255;
					this.match(SparkSqlParser.BY);
					this.state = 2256;
					(_localctx as RowFormatDelimitedContext)._keysTerminatedBy = this.match(SparkSqlParser.STRING);
					}
					break;
				}
				this.state = 2263;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 284, this._ctx) ) {
				case 1:
					{
					this.state = 2259;
					this.match(SparkSqlParser.LINES);
					this.state = 2260;
					this.match(SparkSqlParser.TERMINATED);
					this.state = 2261;
					this.match(SparkSqlParser.BY);
					this.state = 2262;
					(_localctx as RowFormatDelimitedContext)._linesSeparatedBy = this.match(SparkSqlParser.STRING);
					}
					break;
				}
				this.state = 2269;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 285, this._ctx) ) {
				case 1:
					{
					this.state = 2265;
					this.match(SparkSqlParser.NULL);
					this.state = 2266;
					this.match(SparkSqlParser.DEFINED);
					this.state = 2267;
					this.match(SparkSqlParser.AS);
					this.state = 2268;
					(_localctx as RowFormatDelimitedContext)._nullDefinedAs = this.match(SparkSqlParser.STRING);
					}
					break;
				}
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public multipartIdentifierList(): MultipartIdentifierListContext {
		let _localctx: MultipartIdentifierListContext = new MultipartIdentifierListContext(this._ctx, this.state);
		this.enterRule(_localctx, 174, SparkSqlParser.RULE_multipartIdentifierList);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2273;
			this.multipartIdentifier();
			this.state = 2278;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__2) {
				{
				{
				this.state = 2274;
				this.match(SparkSqlParser.T__2);
				this.state = 2275;
				this.multipartIdentifier();
				}
				}
				this.state = 2280;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public multipartIdentifier(): MultipartIdentifierContext {
		let _localctx: MultipartIdentifierContext = new MultipartIdentifierContext(this._ctx, this.state);
		this.enterRule(_localctx, 176, SparkSqlParser.RULE_multipartIdentifier);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2281;
			_localctx._errorCapturingIdentifier = this.errorCapturingIdentifier();
			_localctx._parts.push(_localctx._errorCapturingIdentifier);
			this.state = 2286;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 288, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					{
					{
					this.state = 2282;
					this.match(SparkSqlParser.T__3);
					this.state = 2283;
					_localctx._errorCapturingIdentifier = this.errorCapturingIdentifier();
					_localctx._parts.push(_localctx._errorCapturingIdentifier);
					}
					}
				}
				this.state = 2288;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 288, this._ctx);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public tableIdentifier(): TableIdentifierContext {
		let _localctx: TableIdentifierContext = new TableIdentifierContext(this._ctx, this.state);
		this.enterRule(_localctx, 178, SparkSqlParser.RULE_tableIdentifier);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2292;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 289, this._ctx) ) {
			case 1:
				{
				this.state = 2289;
				_localctx._db = this.errorCapturingIdentifier();
				this.state = 2290;
				this.match(SparkSqlParser.T__3);
				}
				break;
			}
			this.state = 2294;
			_localctx._table = this.errorCapturingIdentifier();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public namedExpression(): NamedExpressionContext {
		let _localctx: NamedExpressionContext = new NamedExpressionContext(this._ctx, this.state);
		this.enterRule(_localctx, 180, SparkSqlParser.RULE_namedExpression);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2296;
			this.expression();
			this.state = 2304;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 292, this._ctx) ) {
			case 1:
				{
				this.state = 2298;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 290, this._ctx) ) {
				case 1:
					{
					this.state = 2297;
					this.match(SparkSqlParser.AS);
					}
					break;
				}
				this.state = 2302;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 291, this._ctx) ) {
				case 1:
					{
					this.state = 2300;
					_localctx._name = this.errorCapturingIdentifier();
					}
					break;

				case 2:
					{
					this.state = 2301;
					this.identifierList();
					}
					break;
				}
				}
				break;
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public namedExpressionSeq(): NamedExpressionSeqContext {
		let _localctx: NamedExpressionSeqContext = new NamedExpressionSeqContext(this._ctx, this.state);
		this.enterRule(_localctx, 182, SparkSqlParser.RULE_namedExpressionSeq);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2306;
			this.namedExpression();
			this.state = 2311;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 293, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					{
					{
					this.state = 2307;
					this.match(SparkSqlParser.T__2);
					this.state = 2308;
					this.namedExpression();
					}
					}
				}
				this.state = 2313;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 293, this._ctx);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public transformList(): TransformListContext {
		let _localctx: TransformListContext = new TransformListContext(this._ctx, this.state);
		this.enterRule(_localctx, 184, SparkSqlParser.RULE_transformList);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2314;
			this.match(SparkSqlParser.T__0);
			this.state = 2315;
			_localctx._transform = this.transform();
			_localctx._transforms.push(_localctx._transform);
			this.state = 2320;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__2) {
				{
				{
				this.state = 2316;
				this.match(SparkSqlParser.T__2);
				this.state = 2317;
				_localctx._transform = this.transform();
				_localctx._transforms.push(_localctx._transform);
				}
				}
				this.state = 2322;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 2323;
			this.match(SparkSqlParser.T__1);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public transform(): TransformContext {
		let _localctx: TransformContext = new TransformContext(this._ctx, this.state);
		this.enterRule(_localctx, 186, SparkSqlParser.RULE_transform);
		let _la: number;
		try {
			this.state = 2338;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 296, this._ctx) ) {
			case 1:
				_localctx = new IdentityTransformContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2325;
				this.qualifiedName();
				}
				break;

			case 2:
				_localctx = new ApplyTransformContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2326;
				(_localctx as ApplyTransformContext)._transformName = this.identifier();
				this.state = 2327;
				this.match(SparkSqlParser.T__0);
				this.state = 2328;
				(_localctx as ApplyTransformContext)._transformArgument = this.transformArgument();
				(_localctx as ApplyTransformContext)._argument.push((_localctx as ApplyTransformContext)._transformArgument);
				this.state = 2333;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === SparkSqlParser.T__2) {
					{
					{
					this.state = 2329;
					this.match(SparkSqlParser.T__2);
					this.state = 2330;
					(_localctx as ApplyTransformContext)._transformArgument = this.transformArgument();
					(_localctx as ApplyTransformContext)._argument.push((_localctx as ApplyTransformContext)._transformArgument);
					}
					}
					this.state = 2335;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				this.state = 2336;
				this.match(SparkSqlParser.T__1);
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public transformArgument(): TransformArgumentContext {
		let _localctx: TransformArgumentContext = new TransformArgumentContext(this._ctx, this.state);
		this.enterRule(_localctx, 188, SparkSqlParser.RULE_transformArgument);
		try {
			this.state = 2342;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 297, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2340;
				this.qualifiedName();
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2341;
				this.constant();
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public expression(): ExpressionContext {
		let _localctx: ExpressionContext = new ExpressionContext(this._ctx, this.state);
		this.enterRule(_localctx, 190, SparkSqlParser.RULE_expression);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2344;
			this.booleanExpression(0);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}

	public booleanExpression(): BooleanExpressionContext;
	public booleanExpression(_p: number): BooleanExpressionContext;
	// @RuleVersion(0)
	public booleanExpression(_p?: number): BooleanExpressionContext {
		if (_p === undefined) {
			_p = 0;
		}

		let _parentctx: ParserRuleContext = this._ctx;
		let _parentState: number = this.state;
		let _localctx: BooleanExpressionContext = new BooleanExpressionContext(this._ctx, _parentState);
		let _prevctx: BooleanExpressionContext = _localctx;
		let _startState: number = 192;
		this.enterRecursionRule(_localctx, 192, SparkSqlParser.RULE_booleanExpression, _p);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2358;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 299, this._ctx) ) {
			case 1:
				{
				_localctx = new LogicalNotContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;

				this.state = 2347;
				this.match(SparkSqlParser.NOT);
				this.state = 2348;
				this.booleanExpression(5);
				}
				break;

			case 2:
				{
				_localctx = new ExistsContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2349;
				this.match(SparkSqlParser.EXISTS);
				this.state = 2350;
				this.match(SparkSqlParser.T__0);
				this.state = 2351;
				this.query();
				this.state = 2352;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 3:
				{
				_localctx = new PredicatedContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2354;
				this.valueExpression(0);
				this.state = 2356;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 298, this._ctx) ) {
				case 1:
					{
					this.state = 2355;
					this.predicate();
					}
					break;
				}
				}
				break;
			}
			this._ctx._stop = this._input.tryLT(-1);
			this.state = 2368;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 301, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					if (this._parseListeners != null) {
						this.triggerExitRuleEvent();
					}
					_prevctx = _localctx;
					{
					this.state = 2366;
					this._errHandler.sync(this);
					switch ( this.interpreter.adaptivePredict(this._input, 300, this._ctx) ) {
					case 1:
						{
						_localctx = new LogicalBinaryContext(new BooleanExpressionContext(_parentctx, _parentState));
						(_localctx as LogicalBinaryContext)._left = _prevctx;
						this.pushNewRecursionContext(_localctx, _startState, SparkSqlParser.RULE_booleanExpression);
						this.state = 2360;
						if (!(this.precpred(this._ctx, 2))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 2)");
						}
						this.state = 2361;
						(_localctx as LogicalBinaryContext)._operator = this.match(SparkSqlParser.AND);
						this.state = 2362;
						(_localctx as LogicalBinaryContext)._right = this.booleanExpression(3);
						}
						break;

					case 2:
						{
						_localctx = new LogicalBinaryContext(new BooleanExpressionContext(_parentctx, _parentState));
						(_localctx as LogicalBinaryContext)._left = _prevctx;
						this.pushNewRecursionContext(_localctx, _startState, SparkSqlParser.RULE_booleanExpression);
						this.state = 2363;
						if (!(this.precpred(this._ctx, 1))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 1)");
						}
						this.state = 2364;
						(_localctx as LogicalBinaryContext)._operator = this.match(SparkSqlParser.OR);
						this.state = 2365;
						(_localctx as LogicalBinaryContext)._right = this.booleanExpression(2);
						}
						break;
					}
					}
				}
				this.state = 2370;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 301, this._ctx);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public predicate(): PredicateContext {
		let _localctx: PredicateContext = new PredicateContext(this._ctx, this.state);
		this.enterRule(_localctx, 194, SparkSqlParser.RULE_predicate);
		let _la: number;
		try {
			this.state = 2453;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 315, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2372;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.NOT) {
					{
					this.state = 2371;
					this.match(SparkSqlParser.NOT);
					}
				}

				this.state = 2374;
				_localctx._kind = this.match(SparkSqlParser.BETWEEN);
				this.state = 2375;
				_localctx._lower = this.valueExpression(0);
				this.state = 2376;
				this.match(SparkSqlParser.AND);
				this.state = 2377;
				_localctx._upper = this.valueExpression(0);
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2380;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.NOT) {
					{
					this.state = 2379;
					this.match(SparkSqlParser.NOT);
					}
				}

				this.state = 2382;
				_localctx._kind = this.match(SparkSqlParser.IN);
				this.state = 2383;
				this.match(SparkSqlParser.T__0);
				this.state = 2384;
				this.expression();
				this.state = 2389;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la === SparkSqlParser.T__2) {
					{
					{
					this.state = 2385;
					this.match(SparkSqlParser.T__2);
					this.state = 2386;
					this.expression();
					}
					}
					this.state = 2391;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				this.state = 2392;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 3:
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 2395;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.NOT) {
					{
					this.state = 2394;
					this.match(SparkSqlParser.NOT);
					}
				}

				this.state = 2397;
				_localctx._kind = this.match(SparkSqlParser.IN);
				this.state = 2398;
				this.match(SparkSqlParser.T__0);
				this.state = 2399;
				this.query();
				this.state = 2400;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 4:
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 2403;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.NOT) {
					{
					this.state = 2402;
					this.match(SparkSqlParser.NOT);
					}
				}

				this.state = 2405;
				_localctx._kind = this.match(SparkSqlParser.RLIKE);
				this.state = 2406;
				_localctx._pattern = this.valueExpression(0);
				}
				break;

			case 5:
				this.enterOuterAlt(_localctx, 5);
				{
				this.state = 2408;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.NOT) {
					{
					this.state = 2407;
					this.match(SparkSqlParser.NOT);
					}
				}

				this.state = 2410;
				_localctx._kind = this.match(SparkSqlParser.LIKE);
				this.state = 2411;
				_localctx._quantifier = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.ALL || _la === SparkSqlParser.ANY || _la === SparkSqlParser.SOME)) {
					_localctx._quantifier = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 2425;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 309, this._ctx) ) {
				case 1:
					{
					this.state = 2412;
					this.match(SparkSqlParser.T__0);
					this.state = 2413;
					this.match(SparkSqlParser.T__1);
					}
					break;

				case 2:
					{
					this.state = 2414;
					this.match(SparkSqlParser.T__0);
					this.state = 2415;
					this.expression();
					this.state = 2420;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					while (_la === SparkSqlParser.T__2) {
						{
						{
						this.state = 2416;
						this.match(SparkSqlParser.T__2);
						this.state = 2417;
						this.expression();
						}
						}
						this.state = 2422;
						this._errHandler.sync(this);
						_la = this._input.LA(1);
					}
					this.state = 2423;
					this.match(SparkSqlParser.T__1);
					}
					break;
				}
				}
				break;

			case 6:
				this.enterOuterAlt(_localctx, 6);
				{
				this.state = 2428;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.NOT) {
					{
					this.state = 2427;
					this.match(SparkSqlParser.NOT);
					}
				}

				this.state = 2430;
				_localctx._kind = this.match(SparkSqlParser.LIKE);
				this.state = 2431;
				_localctx._pattern = this.valueExpression(0);
				this.state = 2434;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 311, this._ctx) ) {
				case 1:
					{
					this.state = 2432;
					this.match(SparkSqlParser.ESCAPE);
					this.state = 2433;
					_localctx._escapeChar = this.match(SparkSqlParser.STRING);
					}
					break;
				}
				}
				break;

			case 7:
				this.enterOuterAlt(_localctx, 7);
				{
				this.state = 2436;
				this.match(SparkSqlParser.IS);
				this.state = 2438;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.NOT) {
					{
					this.state = 2437;
					this.match(SparkSqlParser.NOT);
					}
				}

				this.state = 2440;
				_localctx._kind = this.match(SparkSqlParser.NULL);
				}
				break;

			case 8:
				this.enterOuterAlt(_localctx, 8);
				{
				this.state = 2441;
				this.match(SparkSqlParser.IS);
				this.state = 2443;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.NOT) {
					{
					this.state = 2442;
					this.match(SparkSqlParser.NOT);
					}
				}

				this.state = 2445;
				_localctx._kind = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.FALSE || _la === SparkSqlParser.TRUE || _la === SparkSqlParser.UNKNOWN)) {
					_localctx._kind = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
				break;

			case 9:
				this.enterOuterAlt(_localctx, 9);
				{
				this.state = 2446;
				this.match(SparkSqlParser.IS);
				this.state = 2448;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.NOT) {
					{
					this.state = 2447;
					this.match(SparkSqlParser.NOT);
					}
				}

				this.state = 2450;
				_localctx._kind = this.match(SparkSqlParser.DISTINCT);
				this.state = 2451;
				this.match(SparkSqlParser.FROM);
				this.state = 2452;
				_localctx._right = this.valueExpression(0);
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}

	public valueExpression(): ValueExpressionContext;
	public valueExpression(_p: number): ValueExpressionContext;
	// @RuleVersion(0)
	public valueExpression(_p?: number): ValueExpressionContext {
		if (_p === undefined) {
			_p = 0;
		}

		let _parentctx: ParserRuleContext = this._ctx;
		let _parentState: number = this.state;
		let _localctx: ValueExpressionContext = new ValueExpressionContext(this._ctx, _parentState);
		let _prevctx: ValueExpressionContext = _localctx;
		let _startState: number = 196;
		this.enterRecursionRule(_localctx, 196, SparkSqlParser.RULE_valueExpression, _p);
		let _la: number;
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2459;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 316, this._ctx) ) {
			case 1:
				{
				_localctx = new ValueExpressionDefaultContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;

				this.state = 2456;
				this.primaryExpression(0);
				}
				break;

			case 2:
				{
				_localctx = new ArithmeticUnaryContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2457;
				(_localctx as ArithmeticUnaryContext)._operator = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(((((_la - 268)) & ~0x1F) === 0 && ((1 << (_la - 268)) & ((1 << (SparkSqlParser.PLUS - 268)) | (1 << (SparkSqlParser.MINUS - 268)) | (1 << (SparkSqlParser.TILDE - 268)))) !== 0))) {
					(_localctx as ArithmeticUnaryContext)._operator = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 2458;
				this.valueExpression(7);
				}
				break;
			}
			this._ctx._stop = this._input.tryLT(-1);
			this.state = 2482;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 318, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					if (this._parseListeners != null) {
						this.triggerExitRuleEvent();
					}
					_prevctx = _localctx;
					{
					this.state = 2480;
					this._errHandler.sync(this);
					switch ( this.interpreter.adaptivePredict(this._input, 317, this._ctx) ) {
					case 1:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						(_localctx as ArithmeticBinaryContext)._left = _prevctx;
						this.pushNewRecursionContext(_localctx, _startState, SparkSqlParser.RULE_valueExpression);
						this.state = 2461;
						if (!(this.precpred(this._ctx, 6))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 6)");
						}
						this.state = 2462;
						(_localctx as ArithmeticBinaryContext)._operator = this._input.LT(1);
						_la = this._input.LA(1);
						if (!(_la === SparkSqlParser.DIV || ((((_la - 270)) & ~0x1F) === 0 && ((1 << (_la - 270)) & ((1 << (SparkSqlParser.ASTERISK - 270)) | (1 << (SparkSqlParser.SLASH - 270)) | (1 << (SparkSqlParser.PERCENT - 270)))) !== 0))) {
							(_localctx as ArithmeticBinaryContext)._operator = this._errHandler.recoverInline(this);
						} else {
							if (this._input.LA(1) === Token.EOF) {
								this.matchedEOF = true;
							}

							this._errHandler.reportMatch(this);
							this.consume();
						}
						this.state = 2463;
						(_localctx as ArithmeticBinaryContext)._right = this.valueExpression(7);
						}
						break;

					case 2:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						(_localctx as ArithmeticBinaryContext)._left = _prevctx;
						this.pushNewRecursionContext(_localctx, _startState, SparkSqlParser.RULE_valueExpression);
						this.state = 2464;
						if (!(this.precpred(this._ctx, 5))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 5)");
						}
						this.state = 2465;
						(_localctx as ArithmeticBinaryContext)._operator = this._input.LT(1);
						_la = this._input.LA(1);
						if (!(((((_la - 268)) & ~0x1F) === 0 && ((1 << (_la - 268)) & ((1 << (SparkSqlParser.PLUS - 268)) | (1 << (SparkSqlParser.MINUS - 268)) | (1 << (SparkSqlParser.CONCAT_PIPE - 268)))) !== 0))) {
							(_localctx as ArithmeticBinaryContext)._operator = this._errHandler.recoverInline(this);
						} else {
							if (this._input.LA(1) === Token.EOF) {
								this.matchedEOF = true;
							}

							this._errHandler.reportMatch(this);
							this.consume();
						}
						this.state = 2466;
						(_localctx as ArithmeticBinaryContext)._right = this.valueExpression(6);
						}
						break;

					case 3:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						(_localctx as ArithmeticBinaryContext)._left = _prevctx;
						this.pushNewRecursionContext(_localctx, _startState, SparkSqlParser.RULE_valueExpression);
						this.state = 2467;
						if (!(this.precpred(this._ctx, 4))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 4)");
						}
						this.state = 2468;
						(_localctx as ArithmeticBinaryContext)._operator = this.match(SparkSqlParser.AMPERSAND);
						this.state = 2469;
						(_localctx as ArithmeticBinaryContext)._right = this.valueExpression(5);
						}
						break;

					case 4:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						(_localctx as ArithmeticBinaryContext)._left = _prevctx;
						this.pushNewRecursionContext(_localctx, _startState, SparkSqlParser.RULE_valueExpression);
						this.state = 2470;
						if (!(this.precpred(this._ctx, 3))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 3)");
						}
						this.state = 2471;
						(_localctx as ArithmeticBinaryContext)._operator = this.match(SparkSqlParser.HAT);
						this.state = 2472;
						(_localctx as ArithmeticBinaryContext)._right = this.valueExpression(4);
						}
						break;

					case 5:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						(_localctx as ArithmeticBinaryContext)._left = _prevctx;
						this.pushNewRecursionContext(_localctx, _startState, SparkSqlParser.RULE_valueExpression);
						this.state = 2473;
						if (!(this.precpred(this._ctx, 2))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 2)");
						}
						this.state = 2474;
						(_localctx as ArithmeticBinaryContext)._operator = this.match(SparkSqlParser.PIPE);
						this.state = 2475;
						(_localctx as ArithmeticBinaryContext)._right = this.valueExpression(3);
						}
						break;

					case 6:
						{
						_localctx = new ComparisonContext(new ValueExpressionContext(_parentctx, _parentState));
						(_localctx as ComparisonContext)._left = _prevctx;
						this.pushNewRecursionContext(_localctx, _startState, SparkSqlParser.RULE_valueExpression);
						this.state = 2476;
						if (!(this.precpred(this._ctx, 1))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 1)");
						}
						this.state = 2477;
						this.comparisonOperator();
						this.state = 2478;
						(_localctx as ComparisonContext)._right = this.valueExpression(2);
						}
						break;
					}
					}
				}
				this.state = 2484;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 318, this._ctx);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public primaryExpression(): PrimaryExpressionContext;
	public primaryExpression(_p: number): PrimaryExpressionContext;
	// @RuleVersion(0)
	public primaryExpression(_p?: number): PrimaryExpressionContext {
		if (_p === undefined) {
			_p = 0;
		}

		let _parentctx: ParserRuleContext = this._ctx;
		let _parentState: number = this.state;
		let _localctx: PrimaryExpressionContext = new PrimaryExpressionContext(this._ctx, _parentState);
		let _prevctx: PrimaryExpressionContext = _localctx;
		let _startState: number = 198;
		this.enterRecursionRule(_localctx, 198, SparkSqlParser.RULE_primaryExpression, _p);
		let _la: number;
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2669;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 338, this._ctx) ) {
			case 1:
				{
				_localctx = new CurrentDatetimeContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;

				this.state = 2486;
				(_localctx as CurrentDatetimeContext)._name = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.CURRENT_DATE || _la === SparkSqlParser.CURRENT_TIMESTAMP)) {
					(_localctx as CurrentDatetimeContext)._name = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
				break;

			case 2:
				{
				_localctx = new SearchedCaseContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2487;
				this.match(SparkSqlParser.CASE);
				this.state = 2489;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				do {
					{
					{
					this.state = 2488;
					this.whenClause();
					}
					}
					this.state = 2491;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				} while (_la === SparkSqlParser.WHEN);
				this.state = 2495;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.ELSE) {
					{
					this.state = 2493;
					this.match(SparkSqlParser.ELSE);
					this.state = 2494;
					(_localctx as SearchedCaseContext)._elseExpression = this.expression();
					}
				}

				this.state = 2497;
				this.match(SparkSqlParser.END);
				}
				break;

			case 3:
				{
				_localctx = new SimpleCaseContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2499;
				this.match(SparkSqlParser.CASE);
				this.state = 2500;
				(_localctx as SimpleCaseContext)._value = this.expression();
				this.state = 2502;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				do {
					{
					{
					this.state = 2501;
					this.whenClause();
					}
					}
					this.state = 2504;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				} while (_la === SparkSqlParser.WHEN);
				this.state = 2508;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.ELSE) {
					{
					this.state = 2506;
					this.match(SparkSqlParser.ELSE);
					this.state = 2507;
					(_localctx as SimpleCaseContext)._elseExpression = this.expression();
					}
				}

				this.state = 2510;
				this.match(SparkSqlParser.END);
				}
				break;

			case 4:
				{
				_localctx = new CastContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2512;
				this.match(SparkSqlParser.CAST);
				this.state = 2513;
				this.match(SparkSqlParser.T__0);
				this.state = 2514;
				this.expression();
				this.state = 2515;
				this.match(SparkSqlParser.AS);
				this.state = 2516;
				this.dataType();
				this.state = 2517;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 5:
				{
				_localctx = new StructContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2519;
				this.match(SparkSqlParser.STRUCT);
				this.state = 2520;
				this.match(SparkSqlParser.T__0);
				this.state = 2529;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 324, this._ctx) ) {
				case 1:
					{
					this.state = 2521;
					(_localctx as StructContext)._namedExpression = this.namedExpression();
					(_localctx as StructContext)._argument.push((_localctx as StructContext)._namedExpression);
					this.state = 2526;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					while (_la === SparkSqlParser.T__2) {
						{
						{
						this.state = 2522;
						this.match(SparkSqlParser.T__2);
						this.state = 2523;
						(_localctx as StructContext)._namedExpression = this.namedExpression();
						(_localctx as StructContext)._argument.push((_localctx as StructContext)._namedExpression);
						}
						}
						this.state = 2528;
						this._errHandler.sync(this);
						_la = this._input.LA(1);
					}
					}
					break;
				}
				this.state = 2531;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 6:
				{
				_localctx = new FirstContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2532;
				this.match(SparkSqlParser.FIRST);
				this.state = 2533;
				this.match(SparkSqlParser.T__0);
				this.state = 2534;
				this.expression();
				this.state = 2537;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.IGNORE) {
					{
					this.state = 2535;
					this.match(SparkSqlParser.IGNORE);
					this.state = 2536;
					this.match(SparkSqlParser.NULLS);
					}
				}

				this.state = 2539;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 7:
				{
				_localctx = new LastContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2541;
				this.match(SparkSqlParser.LAST);
				this.state = 2542;
				this.match(SparkSqlParser.T__0);
				this.state = 2543;
				this.expression();
				this.state = 2546;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.IGNORE) {
					{
					this.state = 2544;
					this.match(SparkSqlParser.IGNORE);
					this.state = 2545;
					this.match(SparkSqlParser.NULLS);
					}
				}

				this.state = 2548;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 8:
				{
				_localctx = new PositionContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2550;
				this.match(SparkSqlParser.POSITION);
				this.state = 2551;
				this.match(SparkSqlParser.T__0);
				this.state = 2552;
				(_localctx as PositionContext)._substr = this.valueExpression(0);
				this.state = 2553;
				this.match(SparkSqlParser.IN);
				this.state = 2554;
				(_localctx as PositionContext)._str = this.valueExpression(0);
				this.state = 2555;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 9:
				{
				_localctx = new ConstantDefaultContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2557;
				this.constant();
				}
				break;

			case 10:
				{
				_localctx = new StarContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2558;
				this.match(SparkSqlParser.ASTERISK);
				}
				break;

			case 11:
				{
				_localctx = new StarContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2559;
				this.qualifiedName();
				this.state = 2560;
				this.match(SparkSqlParser.T__3);
				this.state = 2561;
				this.match(SparkSqlParser.ASTERISK);
				}
				break;

			case 12:
				{
				_localctx = new RowConstructorContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2563;
				this.match(SparkSqlParser.T__0);
				this.state = 2564;
				this.namedExpression();
				this.state = 2567;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				do {
					{
					{
					this.state = 2565;
					this.match(SparkSqlParser.T__2);
					this.state = 2566;
					this.namedExpression();
					}
					}
					this.state = 2569;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				} while (_la === SparkSqlParser.T__2);
				this.state = 2571;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 13:
				{
				_localctx = new SubqueryExpressionContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2573;
				this.match(SparkSqlParser.T__0);
				this.state = 2574;
				this.query();
				this.state = 2575;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 14:
				{
				_localctx = new FunctionCallContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2577;
				this.functionName();
				this.state = 2578;
				this.match(SparkSqlParser.T__0);
				this.state = 2590;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 330, this._ctx) ) {
				case 1:
					{
					this.state = 2580;
					this._errHandler.sync(this);
					switch ( this.interpreter.adaptivePredict(this._input, 328, this._ctx) ) {
					case 1:
						{
						this.state = 2579;
						this.setQuantifier();
						}
						break;
					}
					this.state = 2582;
					(_localctx as FunctionCallContext)._expression = this.expression();
					(_localctx as FunctionCallContext)._argument.push((_localctx as FunctionCallContext)._expression);
					this.state = 2587;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					while (_la === SparkSqlParser.T__2) {
						{
						{
						this.state = 2583;
						this.match(SparkSqlParser.T__2);
						this.state = 2584;
						(_localctx as FunctionCallContext)._expression = this.expression();
						(_localctx as FunctionCallContext)._argument.push((_localctx as FunctionCallContext)._expression);
						}
						}
						this.state = 2589;
						this._errHandler.sync(this);
						_la = this._input.LA(1);
					}
					}
					break;
				}
				this.state = 2592;
				this.match(SparkSqlParser.T__1);
				this.state = 2599;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 331, this._ctx) ) {
				case 1:
					{
					this.state = 2593;
					this.match(SparkSqlParser.FILTER);
					this.state = 2594;
					this.match(SparkSqlParser.T__0);
					this.state = 2595;
					this.match(SparkSqlParser.WHERE);
					this.state = 2596;
					(_localctx as FunctionCallContext)._where = this.booleanExpression(0);
					this.state = 2597;
					this.match(SparkSqlParser.T__1);
					}
					break;
				}
				this.state = 2603;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 332, this._ctx) ) {
				case 1:
					{
					this.state = 2601;
					this.match(SparkSqlParser.OVER);
					this.state = 2602;
					this.windowSpec();
					}
					break;
				}
				}
				break;

			case 15:
				{
				_localctx = new LambdaContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2605;
				this.identifier();
				this.state = 2606;
				this.match(SparkSqlParser.T__6);
				this.state = 2607;
				this.expression();
				}
				break;

			case 16:
				{
				_localctx = new LambdaContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2609;
				this.match(SparkSqlParser.T__0);
				this.state = 2610;
				this.identifier();
				this.state = 2613;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				do {
					{
					{
					this.state = 2611;
					this.match(SparkSqlParser.T__2);
					this.state = 2612;
					this.identifier();
					}
					}
					this.state = 2615;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				} while (_la === SparkSqlParser.T__2);
				this.state = 2617;
				this.match(SparkSqlParser.T__1);
				this.state = 2618;
				this.match(SparkSqlParser.T__6);
				this.state = 2619;
				this.expression();
				}
				break;

			case 17:
				{
				_localctx = new ColumnReferenceContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2621;
				this.identifier();
				}
				break;

			case 18:
				{
				_localctx = new ParenthesizedExpressionContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2622;
				this.match(SparkSqlParser.T__0);
				this.state = 2623;
				this.expression();
				this.state = 2624;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 19:
				{
				_localctx = new ExtractContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2626;
				this.match(SparkSqlParser.EXTRACT);
				this.state = 2627;
				this.match(SparkSqlParser.T__0);
				this.state = 2628;
				(_localctx as ExtractContext)._field = this.identifier();
				this.state = 2629;
				this.match(SparkSqlParser.FROM);
				this.state = 2630;
				(_localctx as ExtractContext)._source = this.valueExpression(0);
				this.state = 2631;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 20:
				{
				_localctx = new SubstringContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2633;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.SUBSTR || _la === SparkSqlParser.SUBSTRING)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 2634;
				this.match(SparkSqlParser.T__0);
				this.state = 2635;
				(_localctx as SubstringContext)._str = this.valueExpression(0);
				this.state = 2636;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.T__2 || _la === SparkSqlParser.FROM)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 2637;
				(_localctx as SubstringContext)._pos = this.valueExpression(0);
				this.state = 2640;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.T__2 || _la === SparkSqlParser.FOR) {
					{
					this.state = 2638;
					_la = this._input.LA(1);
					if (!(_la === SparkSqlParser.T__2 || _la === SparkSqlParser.FOR)) {
					this._errHandler.recoverInline(this);
					} else {
						if (this._input.LA(1) === Token.EOF) {
							this.matchedEOF = true;
						}

						this._errHandler.reportMatch(this);
						this.consume();
					}
					this.state = 2639;
					(_localctx as SubstringContext)._len = this.valueExpression(0);
					}
				}

				this.state = 2642;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 21:
				{
				_localctx = new TrimContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2644;
				this.match(SparkSqlParser.TRIM);
				this.state = 2645;
				this.match(SparkSqlParser.T__0);
				this.state = 2647;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 335, this._ctx) ) {
				case 1:
					{
					this.state = 2646;
					(_localctx as TrimContext)._trimOption = this._input.LT(1);
					_la = this._input.LA(1);
					if (!(_la === SparkSqlParser.BOTH || _la === SparkSqlParser.LEADING || _la === SparkSqlParser.TRAILING)) {
						(_localctx as TrimContext)._trimOption = this._errHandler.recoverInline(this);
					} else {
						if (this._input.LA(1) === Token.EOF) {
							this.matchedEOF = true;
						}

						this._errHandler.reportMatch(this);
						this.consume();
					}
					}
					break;
				}
				this.state = 2650;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 336, this._ctx) ) {
				case 1:
					{
					this.state = 2649;
					(_localctx as TrimContext)._trimStr = this.valueExpression(0);
					}
					break;
				}
				this.state = 2652;
				this.match(SparkSqlParser.FROM);
				this.state = 2653;
				(_localctx as TrimContext)._srcStr = this.valueExpression(0);
				this.state = 2654;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 22:
				{
				_localctx = new OverlayContext(_localctx);
				this._ctx = _localctx;
				_prevctx = _localctx;
				this.state = 2656;
				this.match(SparkSqlParser.OVERLAY);
				this.state = 2657;
				this.match(SparkSqlParser.T__0);
				this.state = 2658;
				(_localctx as OverlayContext)._input = this.valueExpression(0);
				this.state = 2659;
				this.match(SparkSqlParser.PLACING);
				this.state = 2660;
				(_localctx as OverlayContext)._replace = this.valueExpression(0);
				this.state = 2661;
				this.match(SparkSqlParser.FROM);
				this.state = 2662;
				(_localctx as OverlayContext)._position = this.valueExpression(0);
				this.state = 2665;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.FOR) {
					{
					this.state = 2663;
					this.match(SparkSqlParser.FOR);
					this.state = 2664;
					(_localctx as OverlayContext)._length = this.valueExpression(0);
					}
				}

				this.state = 2667;
				this.match(SparkSqlParser.T__1);
				}
				break;
			}
			this._ctx._stop = this._input.tryLT(-1);
			this.state = 2681;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 340, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					if (this._parseListeners != null) {
						this.triggerExitRuleEvent();
					}
					_prevctx = _localctx;
					{
					this.state = 2679;
					this._errHandler.sync(this);
					switch ( this.interpreter.adaptivePredict(this._input, 339, this._ctx) ) {
					case 1:
						{
						_localctx = new SubscriptContext(new PrimaryExpressionContext(_parentctx, _parentState));
						(_localctx as SubscriptContext)._value = _prevctx;
						this.pushNewRecursionContext(_localctx, _startState, SparkSqlParser.RULE_primaryExpression);
						this.state = 2671;
						if (!(this.precpred(this._ctx, 8))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 8)");
						}
						this.state = 2672;
						this.match(SparkSqlParser.T__7);
						this.state = 2673;
						(_localctx as SubscriptContext)._index = this.valueExpression(0);
						this.state = 2674;
						this.match(SparkSqlParser.T__8);
						}
						break;

					case 2:
						{
						_localctx = new DereferenceContext(new PrimaryExpressionContext(_parentctx, _parentState));
						(_localctx as DereferenceContext)._base = _prevctx;
						this.pushNewRecursionContext(_localctx, _startState, SparkSqlParser.RULE_primaryExpression);
						this.state = 2676;
						if (!(this.precpred(this._ctx, 6))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 6)");
						}
						this.state = 2677;
						this.match(SparkSqlParser.T__3);
						this.state = 2678;
						(_localctx as DereferenceContext)._fieldName = this.identifier();
						}
						break;
					}
					}
				}
				this.state = 2683;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 340, this._ctx);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public constant(): ConstantContext {
		let _localctx: ConstantContext = new ConstantContext(this._ctx, this.state);
		this.enterRule(_localctx, 200, SparkSqlParser.RULE_constant);
		try {
			let _alt: number;
			this.state = 2696;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 342, this._ctx) ) {
			case 1:
				_localctx = new NullLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2684;
				this.match(SparkSqlParser.NULL);
				}
				break;

			case 2:
				_localctx = new IntervalLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2685;
				this.interval();
				}
				break;

			case 3:
				_localctx = new TypeConstructorContext(_localctx);
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 2686;
				this.identifier();
				this.state = 2687;
				this.match(SparkSqlParser.STRING);
				}
				break;

			case 4:
				_localctx = new NumericLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 2689;
				this.number();
				}
				break;

			case 5:
				_localctx = new BooleanLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 5);
				{
				this.state = 2690;
				this.booleanValue();
				}
				break;

			case 6:
				_localctx = new StringLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 6);
				{
				this.state = 2692;
				this._errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						this.state = 2691;
						this.match(SparkSqlParser.STRING);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					this.state = 2694;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 341, this._ctx);
				} while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER);
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public comparisonOperator(): ComparisonOperatorContext {
		let _localctx: ComparisonOperatorContext = new ComparisonOperatorContext(this._ctx, this.state);
		this.enterRule(_localctx, 202, SparkSqlParser.RULE_comparisonOperator);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2698;
			_la = this._input.LA(1);
			if (!(((((_la - 260)) & ~0x1F) === 0 && ((1 << (_la - 260)) & ((1 << (SparkSqlParser.EQ - 260)) | (1 << (SparkSqlParser.NSEQ - 260)) | (1 << (SparkSqlParser.NEQ - 260)) | (1 << (SparkSqlParser.NEQJ - 260)) | (1 << (SparkSqlParser.LT - 260)) | (1 << (SparkSqlParser.LTE - 260)) | (1 << (SparkSqlParser.GT - 260)) | (1 << (SparkSqlParser.GTE - 260)))) !== 0))) {
			this._errHandler.recoverInline(this);
			} else {
				if (this._input.LA(1) === Token.EOF) {
					this.matchedEOF = true;
				}

				this._errHandler.reportMatch(this);
				this.consume();
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public arithmeticOperator(): ArithmeticOperatorContext {
		let _localctx: ArithmeticOperatorContext = new ArithmeticOperatorContext(this._ctx, this.state);
		this.enterRule(_localctx, 204, SparkSqlParser.RULE_arithmeticOperator);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2700;
			_la = this._input.LA(1);
			if (!(_la === SparkSqlParser.DIV || ((((_la - 268)) & ~0x1F) === 0 && ((1 << (_la - 268)) & ((1 << (SparkSqlParser.PLUS - 268)) | (1 << (SparkSqlParser.MINUS - 268)) | (1 << (SparkSqlParser.ASTERISK - 268)) | (1 << (SparkSqlParser.SLASH - 268)) | (1 << (SparkSqlParser.PERCENT - 268)) | (1 << (SparkSqlParser.TILDE - 268)) | (1 << (SparkSqlParser.AMPERSAND - 268)) | (1 << (SparkSqlParser.PIPE - 268)) | (1 << (SparkSqlParser.CONCAT_PIPE - 268)) | (1 << (SparkSqlParser.HAT - 268)))) !== 0))) {
			this._errHandler.recoverInline(this);
			} else {
				if (this._input.LA(1) === Token.EOF) {
					this.matchedEOF = true;
				}

				this._errHandler.reportMatch(this);
				this.consume();
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public predicateOperator(): PredicateOperatorContext {
		let _localctx: PredicateOperatorContext = new PredicateOperatorContext(this._ctx, this.state);
		this.enterRule(_localctx, 206, SparkSqlParser.RULE_predicateOperator);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2702;
			_la = this._input.LA(1);
			if (!(_la === SparkSqlParser.AND || _la === SparkSqlParser.IN || _la === SparkSqlParser.NOT || _la === SparkSqlParser.OR)) {
			this._errHandler.recoverInline(this);
			} else {
				if (this._input.LA(1) === Token.EOF) {
					this.matchedEOF = true;
				}

				this._errHandler.reportMatch(this);
				this.consume();
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public booleanValue(): BooleanValueContext {
		let _localctx: BooleanValueContext = new BooleanValueContext(this._ctx, this.state);
		this.enterRule(_localctx, 208, SparkSqlParser.RULE_booleanValue);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2704;
			_la = this._input.LA(1);
			if (!(_la === SparkSqlParser.FALSE || _la === SparkSqlParser.TRUE)) {
			this._errHandler.recoverInline(this);
			} else {
				if (this._input.LA(1) === Token.EOF) {
					this.matchedEOF = true;
				}

				this._errHandler.reportMatch(this);
				this.consume();
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public interval(): IntervalContext {
		let _localctx: IntervalContext = new IntervalContext(this._ctx, this.state);
		this.enterRule(_localctx, 210, SparkSqlParser.RULE_interval);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2706;
			this.match(SparkSqlParser.INTERVAL);
			this.state = 2709;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 343, this._ctx) ) {
			case 1:
				{
				this.state = 2707;
				this.errorCapturingMultiUnitsInterval();
				}
				break;

			case 2:
				{
				this.state = 2708;
				this.errorCapturingUnitToUnitInterval();
				}
				break;
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public errorCapturingMultiUnitsInterval(): ErrorCapturingMultiUnitsIntervalContext {
		let _localctx: ErrorCapturingMultiUnitsIntervalContext = new ErrorCapturingMultiUnitsIntervalContext(this._ctx, this.state);
		this.enterRule(_localctx, 212, SparkSqlParser.RULE_errorCapturingMultiUnitsInterval);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2711;
			this.multiUnitsInterval();
			this.state = 2713;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 344, this._ctx) ) {
			case 1:
				{
				this.state = 2712;
				this.unitToUnitInterval();
				}
				break;
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public multiUnitsInterval(): MultiUnitsIntervalContext {
		let _localctx: MultiUnitsIntervalContext = new MultiUnitsIntervalContext(this._ctx, this.state);
		this.enterRule(_localctx, 214, SparkSqlParser.RULE_multiUnitsInterval);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2718;
			this._errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					this.state = 2715;
					this.intervalValue();
					this.state = 2716;
					_localctx._identifier = this.identifier();
					_localctx._unit.push(_localctx._identifier);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				this.state = 2720;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 345, this._ctx);
			} while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public errorCapturingUnitToUnitInterval(): ErrorCapturingUnitToUnitIntervalContext {
		let _localctx: ErrorCapturingUnitToUnitIntervalContext = new ErrorCapturingUnitToUnitIntervalContext(this._ctx, this.state);
		this.enterRule(_localctx, 216, SparkSqlParser.RULE_errorCapturingUnitToUnitInterval);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2722;
			_localctx._body = this.unitToUnitInterval();
			this.state = 2725;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 346, this._ctx) ) {
			case 1:
				{
				this.state = 2723;
				_localctx._error1 = this.multiUnitsInterval();
				}
				break;

			case 2:
				{
				this.state = 2724;
				_localctx._error2 = this.unitToUnitInterval();
				}
				break;
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public unitToUnitInterval(): UnitToUnitIntervalContext {
		let _localctx: UnitToUnitIntervalContext = new UnitToUnitIntervalContext(this._ctx, this.state);
		this.enterRule(_localctx, 218, SparkSqlParser.RULE_unitToUnitInterval);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2727;
			_localctx._value = this.intervalValue();
			this.state = 2728;
			_localctx._from = this.identifier();
			this.state = 2729;
			this.match(SparkSqlParser.TO);
			this.state = 2730;
			_localctx._to = this.identifier();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public intervalValue(): IntervalValueContext {
		let _localctx: IntervalValueContext = new IntervalValueContext(this._ctx, this.state);
		this.enterRule(_localctx, 220, SparkSqlParser.RULE_intervalValue);
		let _la: number;
		try {
			this.state = 2737;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case SparkSqlParser.PLUS:
			case SparkSqlParser.MINUS:
			case SparkSqlParser.INTEGER_VALUE:
			case SparkSqlParser.DECIMAL_VALUE:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2733;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.PLUS || _la === SparkSqlParser.MINUS) {
					{
					this.state = 2732;
					_la = this._input.LA(1);
					if (!(_la === SparkSqlParser.PLUS || _la === SparkSqlParser.MINUS)) {
					this._errHandler.recoverInline(this);
					} else {
						if (this._input.LA(1) === Token.EOF) {
							this.matchedEOF = true;
						}

						this._errHandler.reportMatch(this);
						this.consume();
					}
					}
				}

				this.state = 2735;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.INTEGER_VALUE || _la === SparkSqlParser.DECIMAL_VALUE)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
				break;
			case SparkSqlParser.STRING:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2736;
				this.match(SparkSqlParser.STRING);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public colPosition(): ColPositionContext {
		let _localctx: ColPositionContext = new ColPositionContext(this._ctx, this.state);
		this.enterRule(_localctx, 222, SparkSqlParser.RULE_colPosition);
		try {
			this.state = 2742;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case SparkSqlParser.FIRST:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2739;
				_localctx._position = this.match(SparkSqlParser.FIRST);
				}
				break;
			case SparkSqlParser.AFTER:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2740;
				_localctx._position = this.match(SparkSqlParser.AFTER);
				this.state = 2741;
				_localctx._afterCol = this.errorCapturingIdentifier();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public dataType(): DataTypeContext {
		let _localctx: DataTypeContext = new DataTypeContext(this._ctx, this.state);
		this.enterRule(_localctx, 224, SparkSqlParser.RULE_dataType);
		let _la: number;
		try {
			this.state = 2778;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 354, this._ctx) ) {
			case 1:
				_localctx = new ComplexDataTypeContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2744;
				(_localctx as ComplexDataTypeContext)._complex = this.match(SparkSqlParser.ARRAY);
				this.state = 2745;
				this.match(SparkSqlParser.LT);
				this.state = 2746;
				this.dataType();
				this.state = 2747;
				this.match(SparkSqlParser.GT);
				}
				break;

			case 2:
				_localctx = new ComplexDataTypeContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2749;
				(_localctx as ComplexDataTypeContext)._complex = this.match(SparkSqlParser.MAP);
				this.state = 2750;
				this.match(SparkSqlParser.LT);
				this.state = 2751;
				this.dataType();
				this.state = 2752;
				this.match(SparkSqlParser.T__2);
				this.state = 2753;
				this.dataType();
				this.state = 2754;
				this.match(SparkSqlParser.GT);
				}
				break;

			case 3:
				_localctx = new ComplexDataTypeContext(_localctx);
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 2756;
				(_localctx as ComplexDataTypeContext)._complex = this.match(SparkSqlParser.STRUCT);
				this.state = 2763;
				this._errHandler.sync(this);
				switch (this._input.LA(1)) {
				case SparkSqlParser.LT:
					{
					this.state = 2757;
					this.match(SparkSqlParser.LT);
					this.state = 2759;
					this._errHandler.sync(this);
					switch ( this.interpreter.adaptivePredict(this._input, 350, this._ctx) ) {
					case 1:
						{
						this.state = 2758;
						this.complexColTypeList();
						}
						break;
					}
					this.state = 2761;
					this.match(SparkSqlParser.GT);
					}
					break;
				case SparkSqlParser.NEQ:
					{
					this.state = 2762;
					this.match(SparkSqlParser.NEQ);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;

			case 4:
				_localctx = new PrimitiveDataTypeContext(_localctx);
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 2765;
				this.identifier();
				this.state = 2776;
				this._errHandler.sync(this);
				switch ( this.interpreter.adaptivePredict(this._input, 353, this._ctx) ) {
				case 1:
					{
					this.state = 2766;
					this.match(SparkSqlParser.T__0);
					this.state = 2767;
					this.match(SparkSqlParser.INTEGER_VALUE);
					this.state = 2772;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					while (_la === SparkSqlParser.T__2) {
						{
						{
						this.state = 2768;
						this.match(SparkSqlParser.T__2);
						this.state = 2769;
						this.match(SparkSqlParser.INTEGER_VALUE);
						}
						}
						this.state = 2774;
						this._errHandler.sync(this);
						_la = this._input.LA(1);
					}
					this.state = 2775;
					this.match(SparkSqlParser.T__1);
					}
					break;
				}
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public qualifiedColTypeWithPositionList(): QualifiedColTypeWithPositionListContext {
		let _localctx: QualifiedColTypeWithPositionListContext = new QualifiedColTypeWithPositionListContext(this._ctx, this.state);
		this.enterRule(_localctx, 226, SparkSqlParser.RULE_qualifiedColTypeWithPositionList);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2780;
			this.qualifiedColTypeWithPosition();
			this.state = 2785;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__2) {
				{
				{
				this.state = 2781;
				this.match(SparkSqlParser.T__2);
				this.state = 2782;
				this.qualifiedColTypeWithPosition();
				}
				}
				this.state = 2787;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public qualifiedColTypeWithPosition(): QualifiedColTypeWithPositionContext {
		let _localctx: QualifiedColTypeWithPositionContext = new QualifiedColTypeWithPositionContext(this._ctx, this.state);
		this.enterRule(_localctx, 228, SparkSqlParser.RULE_qualifiedColTypeWithPosition);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2788;
			_localctx._name = this.multipartIdentifier();
			this.state = 2789;
			this.dataType();
			this.state = 2792;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.NOT) {
				{
				this.state = 2790;
				this.match(SparkSqlParser.NOT);
				this.state = 2791;
				this.match(SparkSqlParser.NULL);
				}
			}

			this.state = 2795;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 357, this._ctx) ) {
			case 1:
				{
				this.state = 2794;
				this.commentSpec();
				}
				break;
			}
			this.state = 2798;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.AFTER || _la === SparkSqlParser.FIRST) {
				{
				this.state = 2797;
				this.colPosition();
				}
			}

			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public colTypeList(): ColTypeListContext {
		let _localctx: ColTypeListContext = new ColTypeListContext(this._ctx, this.state);
		this.enterRule(_localctx, 230, SparkSqlParser.RULE_colTypeList);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2800;
			this.colType();
			this.state = 2805;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 359, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					{
					{
					this.state = 2801;
					this.match(SparkSqlParser.T__2);
					this.state = 2802;
					this.colType();
					}
					}
				}
				this.state = 2807;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 359, this._ctx);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public colType(): ColTypeContext {
		let _localctx: ColTypeContext = new ColTypeContext(this._ctx, this.state);
		this.enterRule(_localctx, 232, SparkSqlParser.RULE_colType);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2808;
			_localctx._colName = this.errorCapturingIdentifier();
			this.state = 2809;
			this.dataType();
			this.state = 2812;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 360, this._ctx) ) {
			case 1:
				{
				this.state = 2810;
				this.match(SparkSqlParser.NOT);
				this.state = 2811;
				this.match(SparkSqlParser.NULL);
				}
				break;
			}
			this.state = 2815;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 361, this._ctx) ) {
			case 1:
				{
				this.state = 2814;
				this.commentSpec();
				}
				break;
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public complexColTypeList(): ComplexColTypeListContext {
		let _localctx: ComplexColTypeListContext = new ComplexColTypeListContext(this._ctx, this.state);
		this.enterRule(_localctx, 234, SparkSqlParser.RULE_complexColTypeList);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2817;
			this.complexColType();
			this.state = 2822;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__2) {
				{
				{
				this.state = 2818;
				this.match(SparkSqlParser.T__2);
				this.state = 2819;
				this.complexColType();
				}
				}
				this.state = 2824;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public complexColType(): ComplexColTypeContext {
		let _localctx: ComplexColTypeContext = new ComplexColTypeContext(this._ctx, this.state);
		this.enterRule(_localctx, 236, SparkSqlParser.RULE_complexColType);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2825;
			this.identifier();
			this.state = 2826;
			this.match(SparkSqlParser.T__9);
			this.state = 2827;
			this.dataType();
			this.state = 2830;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.NOT) {
				{
				this.state = 2828;
				this.match(SparkSqlParser.NOT);
				this.state = 2829;
				this.match(SparkSqlParser.NULL);
				}
			}

			this.state = 2833;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === SparkSqlParser.COMMENT) {
				{
				this.state = 2832;
				this.commentSpec();
				}
			}

			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public whenClause(): WhenClauseContext {
		let _localctx: WhenClauseContext = new WhenClauseContext(this._ctx, this.state);
		this.enterRule(_localctx, 238, SparkSqlParser.RULE_whenClause);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2835;
			this.match(SparkSqlParser.WHEN);
			this.state = 2836;
			_localctx._condition = this.expression();
			this.state = 2837;
			this.match(SparkSqlParser.THEN);
			this.state = 2838;
			_localctx._result = this.expression();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public windowClause(): WindowClauseContext {
		let _localctx: WindowClauseContext = new WindowClauseContext(this._ctx, this.state);
		this.enterRule(_localctx, 240, SparkSqlParser.RULE_windowClause);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2840;
			this.match(SparkSqlParser.WINDOW);
			this.state = 2841;
			this.namedWindow();
			this.state = 2846;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 365, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					{
					{
					this.state = 2842;
					this.match(SparkSqlParser.T__2);
					this.state = 2843;
					this.namedWindow();
					}
					}
				}
				this.state = 2848;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 365, this._ctx);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public namedWindow(): NamedWindowContext {
		let _localctx: NamedWindowContext = new NamedWindowContext(this._ctx, this.state);
		this.enterRule(_localctx, 242, SparkSqlParser.RULE_namedWindow);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2849;
			_localctx._name = this.errorCapturingIdentifier();
			this.state = 2850;
			this.match(SparkSqlParser.AS);
			this.state = 2851;
			this.windowSpec();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public windowSpec(): WindowSpecContext {
		let _localctx: WindowSpecContext = new WindowSpecContext(this._ctx, this.state);
		this.enterRule(_localctx, 244, SparkSqlParser.RULE_windowSpec);
		let _la: number;
		try {
			this.state = 2899;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 373, this._ctx) ) {
			case 1:
				_localctx = new WindowRefContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2853;
				(_localctx as WindowRefContext)._name = this.errorCapturingIdentifier();
				}
				break;

			case 2:
				_localctx = new WindowRefContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2854;
				this.match(SparkSqlParser.T__0);
				this.state = 2855;
				(_localctx as WindowRefContext)._name = this.errorCapturingIdentifier();
				this.state = 2856;
				this.match(SparkSqlParser.T__1);
				}
				break;

			case 3:
				_localctx = new WindowDefContext(_localctx);
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 2858;
				this.match(SparkSqlParser.T__0);
				this.state = 2893;
				this._errHandler.sync(this);
				switch (this._input.LA(1)) {
				case SparkSqlParser.CLUSTER:
					{
					this.state = 2859;
					this.match(SparkSqlParser.CLUSTER);
					this.state = 2860;
					this.match(SparkSqlParser.BY);
					this.state = 2861;
					(_localctx as WindowDefContext)._expression = this.expression();
					(_localctx as WindowDefContext)._partition.push((_localctx as WindowDefContext)._expression);
					this.state = 2866;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					while (_la === SparkSqlParser.T__2) {
						{
						{
						this.state = 2862;
						this.match(SparkSqlParser.T__2);
						this.state = 2863;
						(_localctx as WindowDefContext)._expression = this.expression();
						(_localctx as WindowDefContext)._partition.push((_localctx as WindowDefContext)._expression);
						}
						}
						this.state = 2868;
						this._errHandler.sync(this);
						_la = this._input.LA(1);
					}
					}
					break;
				case SparkSqlParser.T__1:
				case SparkSqlParser.DISTRIBUTE:
				case SparkSqlParser.ORDER:
				case SparkSqlParser.PARTITION:
				case SparkSqlParser.RANGE:
				case SparkSqlParser.ROWS:
				case SparkSqlParser.SORT:
					{
					this.state = 2879;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					if (_la === SparkSqlParser.DISTRIBUTE || _la === SparkSqlParser.PARTITION) {
						{
						this.state = 2869;
						_la = this._input.LA(1);
						if (!(_la === SparkSqlParser.DISTRIBUTE || _la === SparkSqlParser.PARTITION)) {
						this._errHandler.recoverInline(this);
						} else {
							if (this._input.LA(1) === Token.EOF) {
								this.matchedEOF = true;
							}

							this._errHandler.reportMatch(this);
							this.consume();
						}
						this.state = 2870;
						this.match(SparkSqlParser.BY);
						this.state = 2871;
						(_localctx as WindowDefContext)._expression = this.expression();
						(_localctx as WindowDefContext)._partition.push((_localctx as WindowDefContext)._expression);
						this.state = 2876;
						this._errHandler.sync(this);
						_la = this._input.LA(1);
						while (_la === SparkSqlParser.T__2) {
							{
							{
							this.state = 2872;
							this.match(SparkSqlParser.T__2);
							this.state = 2873;
							(_localctx as WindowDefContext)._expression = this.expression();
							(_localctx as WindowDefContext)._partition.push((_localctx as WindowDefContext)._expression);
							}
							}
							this.state = 2878;
							this._errHandler.sync(this);
							_la = this._input.LA(1);
						}
						}
					}

					this.state = 2891;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
					if (_la === SparkSqlParser.ORDER || _la === SparkSqlParser.SORT) {
						{
						this.state = 2881;
						_la = this._input.LA(1);
						if (!(_la === SparkSqlParser.ORDER || _la === SparkSqlParser.SORT)) {
						this._errHandler.recoverInline(this);
						} else {
							if (this._input.LA(1) === Token.EOF) {
								this.matchedEOF = true;
							}

							this._errHandler.reportMatch(this);
							this.consume();
						}
						this.state = 2882;
						this.match(SparkSqlParser.BY);
						this.state = 2883;
						this.sortItem();
						this.state = 2888;
						this._errHandler.sync(this);
						_la = this._input.LA(1);
						while (_la === SparkSqlParser.T__2) {
							{
							{
							this.state = 2884;
							this.match(SparkSqlParser.T__2);
							this.state = 2885;
							this.sortItem();
							}
							}
							this.state = 2890;
							this._errHandler.sync(this);
							_la = this._input.LA(1);
						}
						}
					}

					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				this.state = 2896;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.RANGE || _la === SparkSqlParser.ROWS) {
					{
					this.state = 2895;
					this.windowFrame();
					}
				}

				this.state = 2898;
				this.match(SparkSqlParser.T__1);
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public windowFrame(): WindowFrameContext {
		let _localctx: WindowFrameContext = new WindowFrameContext(this._ctx, this.state);
		this.enterRule(_localctx, 246, SparkSqlParser.RULE_windowFrame);
		try {
			this.state = 2917;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 374, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2901;
				_localctx._frameType = this.match(SparkSqlParser.RANGE);
				this.state = 2902;
				_localctx._start = this.frameBound();
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2903;
				_localctx._frameType = this.match(SparkSqlParser.ROWS);
				this.state = 2904;
				_localctx._start = this.frameBound();
				}
				break;

			case 3:
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 2905;
				_localctx._frameType = this.match(SparkSqlParser.RANGE);
				this.state = 2906;
				this.match(SparkSqlParser.BETWEEN);
				this.state = 2907;
				_localctx._start = this.frameBound();
				this.state = 2908;
				this.match(SparkSqlParser.AND);
				this.state = 2909;
				_localctx._end = this.frameBound();
				}
				break;

			case 4:
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 2911;
				_localctx._frameType = this.match(SparkSqlParser.ROWS);
				this.state = 2912;
				this.match(SparkSqlParser.BETWEEN);
				this.state = 2913;
				_localctx._start = this.frameBound();
				this.state = 2914;
				this.match(SparkSqlParser.AND);
				this.state = 2915;
				_localctx._end = this.frameBound();
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public frameBound(): FrameBoundContext {
		let _localctx: FrameBoundContext = new FrameBoundContext(this._ctx, this.state);
		this.enterRule(_localctx, 248, SparkSqlParser.RULE_frameBound);
		let _la: number;
		try {
			this.state = 2926;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 375, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2919;
				this.match(SparkSqlParser.UNBOUNDED);
				this.state = 2920;
				_localctx._boundType = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.FOLLOWING || _la === SparkSqlParser.PRECEDING)) {
					_localctx._boundType = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2921;
				_localctx._boundType = this.match(SparkSqlParser.CURRENT);
				this.state = 2922;
				this.match(SparkSqlParser.ROW);
				}
				break;

			case 3:
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 2923;
				this.expression();
				this.state = 2924;
				_localctx._boundType = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.FOLLOWING || _la === SparkSqlParser.PRECEDING)) {
					_localctx._boundType = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public qualifiedNameList(): QualifiedNameListContext {
		let _localctx: QualifiedNameListContext = new QualifiedNameListContext(this._ctx, this.state);
		this.enterRule(_localctx, 250, SparkSqlParser.RULE_qualifiedNameList);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2928;
			this.qualifiedName();
			this.state = 2933;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === SparkSqlParser.T__2) {
				{
				{
				this.state = 2929;
				this.match(SparkSqlParser.T__2);
				this.state = 2930;
				this.qualifiedName();
				}
				}
				this.state = 2935;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public functionName(): FunctionNameContext {
		let _localctx: FunctionNameContext = new FunctionNameContext(this._ctx, this.state);
		this.enterRule(_localctx, 252, SparkSqlParser.RULE_functionName);
		try {
			this.state = 2940;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 377, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2936;
				this.qualifiedName();
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2937;
				this.match(SparkSqlParser.FILTER);
				}
				break;

			case 3:
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 2938;
				this.match(SparkSqlParser.LEFT);
				}
				break;

			case 4:
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 2939;
				this.match(SparkSqlParser.RIGHT);
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public qualifiedName(): QualifiedNameContext {
		let _localctx: QualifiedNameContext = new QualifiedNameContext(this._ctx, this.state);
		this.enterRule(_localctx, 254, SparkSqlParser.RULE_qualifiedName);
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2942;
			this.identifier();
			this.state = 2947;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 378, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					{
					{
					this.state = 2943;
					this.match(SparkSqlParser.T__3);
					this.state = 2944;
					this.identifier();
					}
					}
				}
				this.state = 2949;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 378, this._ctx);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public errorCapturingIdentifier(): ErrorCapturingIdentifierContext {
		let _localctx: ErrorCapturingIdentifierContext = new ErrorCapturingIdentifierContext(this._ctx, this.state);
		this.enterRule(_localctx, 256, SparkSqlParser.RULE_errorCapturingIdentifier);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2950;
			this.identifier();
			this.state = 2951;
			this.errorCapturingIdentifierExtra();
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public errorCapturingIdentifierExtra(): ErrorCapturingIdentifierExtraContext {
		let _localctx: ErrorCapturingIdentifierExtraContext = new ErrorCapturingIdentifierExtraContext(this._ctx, this.state);
		this.enterRule(_localctx, 258, SparkSqlParser.RULE_errorCapturingIdentifierExtra);
		try {
			let _alt: number;
			this.state = 2960;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 380, this._ctx) ) {
			case 1:
				_localctx = new ErrorIdentContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2955;
				this._errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						this.state = 2953;
						this.match(SparkSqlParser.MINUS);
						this.state = 2954;
						this.identifier();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					this.state = 2957;
					this._errHandler.sync(this);
					_alt = this.interpreter.adaptivePredict(this._input, 379, this._ctx);
				} while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER);
				}
				break;

			case 2:
				_localctx = new RealIdentContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				// tslint:disable-next-line:no-empty
				{
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public identifier(): IdentifierContext {
		let _localctx: IdentifierContext = new IdentifierContext(this._ctx, this.state);
		this.enterRule(_localctx, 260, SparkSqlParser.RULE_identifier);
		try {
			this.state = 2965;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 381, this._ctx) ) {
			case 1:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2962;
				this.strictIdentifier();
				}
				break;

			case 2:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2963;
				if (!(!SQL_standard_keyword_behavior)) {
					throw this.createFailedPredicateException("!SQL_standard_keyword_behavior");
				}
				this.state = 2964;
				this.strictNonReserved();
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public strictIdentifier(): StrictIdentifierContext {
		let _localctx: StrictIdentifierContext = new StrictIdentifierContext(this._ctx, this.state);
		this.enterRule(_localctx, 262, SparkSqlParser.RULE_strictIdentifier);
		try {
			this.state = 2973;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 382, this._ctx) ) {
			case 1:
				_localctx = new UnquotedIdentifierContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2967;
				this.match(SparkSqlParser.IDENTIFIER);
				}
				break;

			case 2:
				_localctx = new QuotedIdentifierAlternativeContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2968;
				this.quotedIdentifier();
				}
				break;

			case 3:
				_localctx = new UnquotedIdentifierContext(_localctx);
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 2969;
				if (!(SQL_standard_keyword_behavior)) {
					throw this.createFailedPredicateException("SQL_standard_keyword_behavior");
				}
				this.state = 2970;
				this.ansiNonReserved();
				}
				break;

			case 4:
				_localctx = new UnquotedIdentifierContext(_localctx);
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 2971;
				if (!(!SQL_standard_keyword_behavior)) {
					throw this.createFailedPredicateException("!SQL_standard_keyword_behavior");
				}
				this.state = 2972;
				this.nonReserved();
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public quotedIdentifier(): QuotedIdentifierContext {
		let _localctx: QuotedIdentifierContext = new QuotedIdentifierContext(this._ctx, this.state);
		this.enterRule(_localctx, 264, SparkSqlParser.RULE_quotedIdentifier);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 2975;
			this.match(SparkSqlParser.BACKQUOTED_IDENTIFIER);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public number(): NumberContext {
		let _localctx: NumberContext = new NumberContext(this._ctx, this.state);
		this.enterRule(_localctx, 266, SparkSqlParser.RULE_number);
		let _la: number;
		try {
			this.state = 3020;
			this._errHandler.sync(this);
			switch ( this.interpreter.adaptivePredict(this._input, 393, this._ctx) ) {
			case 1:
				_localctx = new ExponentLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 2977;
				if (!(!legacy_exponent_literal_as_decimal_enabled)) {
					throw this.createFailedPredicateException("!legacy_exponent_literal_as_decimal_enabled");
				}
				this.state = 2979;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.MINUS) {
					{
					this.state = 2978;
					this.match(SparkSqlParser.MINUS);
					}
				}

				this.state = 2981;
				this.match(SparkSqlParser.EXPONENT_VALUE);
				}
				break;

			case 2:
				_localctx = new DecimalLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 2982;
				if (!(!legacy_exponent_literal_as_decimal_enabled)) {
					throw this.createFailedPredicateException("!legacy_exponent_literal_as_decimal_enabled");
				}
				this.state = 2984;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.MINUS) {
					{
					this.state = 2983;
					this.match(SparkSqlParser.MINUS);
					}
				}

				this.state = 2986;
				this.match(SparkSqlParser.DECIMAL_VALUE);
				}
				break;

			case 3:
				_localctx = new LegacyDecimalLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 2987;
				if (!(legacy_exponent_literal_as_decimal_enabled)) {
					throw this.createFailedPredicateException("legacy_exponent_literal_as_decimal_enabled");
				}
				this.state = 2989;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.MINUS) {
					{
					this.state = 2988;
					this.match(SparkSqlParser.MINUS);
					}
				}

				this.state = 2991;
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.EXPONENT_VALUE || _la === SparkSqlParser.DECIMAL_VALUE)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
				break;

			case 4:
				_localctx = new IntegerLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 2993;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.MINUS) {
					{
					this.state = 2992;
					this.match(SparkSqlParser.MINUS);
					}
				}

				this.state = 2995;
				this.match(SparkSqlParser.INTEGER_VALUE);
				}
				break;

			case 5:
				_localctx = new BigIntLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 5);
				{
				this.state = 2997;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.MINUS) {
					{
					this.state = 2996;
					this.match(SparkSqlParser.MINUS);
					}
				}

				this.state = 2999;
				this.match(SparkSqlParser.BIGINT_LITERAL);
				}
				break;

			case 6:
				_localctx = new SmallIntLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 6);
				{
				this.state = 3001;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.MINUS) {
					{
					this.state = 3000;
					this.match(SparkSqlParser.MINUS);
					}
				}

				this.state = 3003;
				this.match(SparkSqlParser.SMALLINT_LITERAL);
				}
				break;

			case 7:
				_localctx = new TinyIntLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 7);
				{
				this.state = 3005;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.MINUS) {
					{
					this.state = 3004;
					this.match(SparkSqlParser.MINUS);
					}
				}

				this.state = 3007;
				this.match(SparkSqlParser.TINYINT_LITERAL);
				}
				break;

			case 8:
				_localctx = new DoubleLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 8);
				{
				this.state = 3009;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.MINUS) {
					{
					this.state = 3008;
					this.match(SparkSqlParser.MINUS);
					}
				}

				this.state = 3011;
				this.match(SparkSqlParser.DOUBLE_LITERAL);
				}
				break;

			case 9:
				_localctx = new FloatLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 9);
				{
				this.state = 3013;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.MINUS) {
					{
					this.state = 3012;
					this.match(SparkSqlParser.MINUS);
					}
				}

				this.state = 3015;
				this.match(SparkSqlParser.FLOAT_LITERAL);
				}
				break;

			case 10:
				_localctx = new BigDecimalLiteralContext(_localctx);
				this.enterOuterAlt(_localctx, 10);
				{
				this.state = 3017;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				if (_la === SparkSqlParser.MINUS) {
					{
					this.state = 3016;
					this.match(SparkSqlParser.MINUS);
					}
				}

				this.state = 3019;
				this.match(SparkSqlParser.BIGDECIMAL_LITERAL);
				}
				break;
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public alterColumnAction(): AlterColumnActionContext {
		let _localctx: AlterColumnActionContext = new AlterColumnActionContext(this._ctx, this.state);
		this.enterRule(_localctx, 268, SparkSqlParser.RULE_alterColumnAction);
		let _la: number;
		try {
			this.state = 3029;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case SparkSqlParser.TYPE:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 3022;
				this.match(SparkSqlParser.TYPE);
				this.state = 3023;
				this.dataType();
				}
				break;
			case SparkSqlParser.COMMENT:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 3024;
				this.commentSpec();
				}
				break;
			case SparkSqlParser.AFTER:
			case SparkSqlParser.FIRST:
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 3025;
				this.colPosition();
				}
				break;
			case SparkSqlParser.DROP:
			case SparkSqlParser.SET:
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 3026;
				_localctx._setOrDrop = this._input.LT(1);
				_la = this._input.LA(1);
				if (!(_la === SparkSqlParser.DROP || _la === SparkSqlParser.SET)) {
					_localctx._setOrDrop = this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 3027;
				this.match(SparkSqlParser.NOT);
				this.state = 3028;
				this.match(SparkSqlParser.NULL);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public ansiNonReserved(): AnsiNonReservedContext {
		let _localctx: AnsiNonReservedContext = new AnsiNonReservedContext(this._ctx, this.state);
		this.enterRule(_localctx, 270, SparkSqlParser.RULE_ansiNonReserved);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 3031;
			_la = this._input.LA(1);
			if (!(((((_la - 11)) & ~0x1F) === 0 && ((1 << (_la - 11)) & ((1 << (SparkSqlParser.ADD - 11)) | (1 << (SparkSqlParser.AFTER - 11)) | (1 << (SparkSqlParser.ALTER - 11)) | (1 << (SparkSqlParser.ANALYZE - 11)) | (1 << (SparkSqlParser.ANTI - 11)) | (1 << (SparkSqlParser.ARCHIVE - 11)) | (1 << (SparkSqlParser.ARRAY - 11)) | (1 << (SparkSqlParser.ASC - 11)) | (1 << (SparkSqlParser.AT - 11)) | (1 << (SparkSqlParser.BETWEEN - 11)) | (1 << (SparkSqlParser.BUCKET - 11)) | (1 << (SparkSqlParser.BUCKETS - 11)) | (1 << (SparkSqlParser.BY - 11)) | (1 << (SparkSqlParser.CACHE - 11)) | (1 << (SparkSqlParser.CASCADE - 11)) | (1 << (SparkSqlParser.CHANGE - 11)) | (1 << (SparkSqlParser.CLEAR - 11)) | (1 << (SparkSqlParser.CLUSTER - 11)) | (1 << (SparkSqlParser.CLUSTERED - 11)) | (1 << (SparkSqlParser.CODEGEN - 11)) | (1 << (SparkSqlParser.COLLECTION - 11)))) !== 0) || ((((_la - 43)) & ~0x1F) === 0 && ((1 << (_la - 43)) & ((1 << (SparkSqlParser.COLUMNS - 43)) | (1 << (SparkSqlParser.COMMENT - 43)) | (1 << (SparkSqlParser.COMMIT - 43)) | (1 << (SparkSqlParser.COMPACT - 43)) | (1 << (SparkSqlParser.COMPACTIONS - 43)) | (1 << (SparkSqlParser.COMPUTE - 43)) | (1 << (SparkSqlParser.CONCATENATE - 43)) | (1 << (SparkSqlParser.COST - 43)) | (1 << (SparkSqlParser.CUBE - 43)) | (1 << (SparkSqlParser.CURRENT - 43)) | (1 << (SparkSqlParser.DATA - 43)) | (1 << (SparkSqlParser.DATABASE - 43)) | (1 << (SparkSqlParser.DATABASES - 43)) | (1 << (SparkSqlParser.DBPROPERTIES - 43)) | (1 << (SparkSqlParser.DEFINED - 43)) | (1 << (SparkSqlParser.DELETE - 43)) | (1 << (SparkSqlParser.DELIMITED - 43)) | (1 << (SparkSqlParser.DESC - 43)) | (1 << (SparkSqlParser.DESCRIBE - 43)) | (1 << (SparkSqlParser.DFS - 43)) | (1 << (SparkSqlParser.DIRECTORIES - 43)) | (1 << (SparkSqlParser.DIRECTORY - 43)) | (1 << (SparkSqlParser.DISTRIBUTE - 43)) | (1 << (SparkSqlParser.DIV - 43)))) !== 0) || ((((_la - 75)) & ~0x1F) === 0 && ((1 << (_la - 75)) & ((1 << (SparkSqlParser.DROP - 75)) | (1 << (SparkSqlParser.ESCAPED - 75)) | (1 << (SparkSqlParser.EXCHANGE - 75)) | (1 << (SparkSqlParser.EXISTS - 75)) | (1 << (SparkSqlParser.EXPLAIN - 75)) | (1 << (SparkSqlParser.EXPORT - 75)) | (1 << (SparkSqlParser.EXTENDED - 75)) | (1 << (SparkSqlParser.EXTERNAL - 75)) | (1 << (SparkSqlParser.EXTRACT - 75)) | (1 << (SparkSqlParser.FIELDS - 75)) | (1 << (SparkSqlParser.FILEFORMAT - 75)) | (1 << (SparkSqlParser.FIRST - 75)) | (1 << (SparkSqlParser.FOLLOWING - 75)) | (1 << (SparkSqlParser.FORMAT - 75)) | (1 << (SparkSqlParser.FORMATTED - 75)) | (1 << (SparkSqlParser.FUNCTION - 75)) | (1 << (SparkSqlParser.FUNCTIONS - 75)) | (1 << (SparkSqlParser.GLOBAL - 75)) | (1 << (SparkSqlParser.GROUPING - 75)))) !== 0) || ((((_la - 108)) & ~0x1F) === 0 && ((1 << (_la - 108)) & ((1 << (SparkSqlParser.IF - 108)) | (1 << (SparkSqlParser.IGNORE - 108)) | (1 << (SparkSqlParser.IMPORT - 108)) | (1 << (SparkSqlParser.INDEX - 108)) | (1 << (SparkSqlParser.INDEXES - 108)) | (1 << (SparkSqlParser.INPATH - 108)) | (1 << (SparkSqlParser.INPUTFORMAT - 108)) | (1 << (SparkSqlParser.INSERT - 108)) | (1 << (SparkSqlParser.INTERVAL - 108)) | (1 << (SparkSqlParser.ITEMS - 108)) | (1 << (SparkSqlParser.KEYS - 108)) | (1 << (SparkSqlParser.LAST - 108)) | (1 << (SparkSqlParser.LATERAL - 108)) | (1 << (SparkSqlParser.LAZY - 108)) | (1 << (SparkSqlParser.LIKE - 108)) | (1 << (SparkSqlParser.LIMIT - 108)) | (1 << (SparkSqlParser.LINES - 108)) | (1 << (SparkSqlParser.LIST - 108)) | (1 << (SparkSqlParser.LOAD - 108)) | (1 << (SparkSqlParser.LOCAL - 108)) | (1 << (SparkSqlParser.LOCATION - 108)) | (1 << (SparkSqlParser.LOCK - 108)) | (1 << (SparkSqlParser.LOCKS - 108)) | (1 << (SparkSqlParser.LOGICAL - 108)))) !== 0) || ((((_la - 140)) & ~0x1F) === 0 && ((1 << (_la - 140)) & ((1 << (SparkSqlParser.MACRO - 140)) | (1 << (SparkSqlParser.MAP - 140)) | (1 << (SparkSqlParser.MATCHED - 140)) | (1 << (SparkSqlParser.MERGE - 140)) | (1 << (SparkSqlParser.MSCK - 140)) | (1 << (SparkSqlParser.NAMESPACE - 140)) | (1 << (SparkSqlParser.NAMESPACES - 140)) | (1 << (SparkSqlParser.NO - 140)) | (1 << (SparkSqlParser.NULLS - 140)) | (1 << (SparkSqlParser.OF - 140)) | (1 << (SparkSqlParser.OPTION - 140)) | (1 << (SparkSqlParser.OPTIONS - 140)) | (1 << (SparkSqlParser.OUT - 140)) | (1 << (SparkSqlParser.OUTPUTFORMAT - 140)) | (1 << (SparkSqlParser.OVER - 140)) | (1 << (SparkSqlParser.OVERLAY - 140)) | (1 << (SparkSqlParser.OVERWRITE - 140)) | (1 << (SparkSqlParser.PARTITION - 140)) | (1 << (SparkSqlParser.PARTITIONED - 140)) | (1 << (SparkSqlParser.PARTITIONS - 140)) | (1 << (SparkSqlParser.PERCENTLIT - 140)) | (1 << (SparkSqlParser.PIVOT - 140)) | (1 << (SparkSqlParser.PLACING - 140)))) !== 0) || ((((_la - 172)) & ~0x1F) === 0 && ((1 << (_la - 172)) & ((1 << (SparkSqlParser.POSITION - 172)) | (1 << (SparkSqlParser.PRECEDING - 172)) | (1 << (SparkSqlParser.PRINCIPALS - 172)) | (1 << (SparkSqlParser.PROPERTIES - 172)) | (1 << (SparkSqlParser.PURGE - 172)) | (1 << (SparkSqlParser.QUERY - 172)) | (1 << (SparkSqlParser.RANGE - 172)) | (1 << (SparkSqlParser.RECORDREADER - 172)) | (1 << (SparkSqlParser.RECORDWRITER - 172)) | (1 << (SparkSqlParser.RECOVER - 172)) | (1 << (SparkSqlParser.REDUCE - 172)) | (1 << (SparkSqlParser.REFRESH - 172)) | (1 << (SparkSqlParser.RENAME - 172)) | (1 << (SparkSqlParser.REPAIR - 172)) | (1 << (SparkSqlParser.REPLACE - 172)) | (1 << (SparkSqlParser.RESET - 172)) | (1 << (SparkSqlParser.RESTRICT - 172)) | (1 << (SparkSqlParser.REVOKE - 172)) | (1 << (SparkSqlParser.RLIKE - 172)) | (1 << (SparkSqlParser.ROLE - 172)) | (1 << (SparkSqlParser.ROLES - 172)) | (1 << (SparkSqlParser.ROLLBACK - 172)) | (1 << (SparkSqlParser.ROLLUP - 172)) | (1 << (SparkSqlParser.ROW - 172)) | (1 << (SparkSqlParser.ROWS - 172)) | (1 << (SparkSqlParser.SCHEMA - 172)) | (1 << (SparkSqlParser.SEMI - 172)) | (1 << (SparkSqlParser.SEPARATED - 172)))) !== 0) || ((((_la - 204)) & ~0x1F) === 0 && ((1 << (_la - 204)) & ((1 << (SparkSqlParser.SERDE - 204)) | (1 << (SparkSqlParser.SERDEPROPERTIES - 204)) | (1 << (SparkSqlParser.SET - 204)) | (1 << (SparkSqlParser.SETMINUS - 204)) | (1 << (SparkSqlParser.SETS - 204)) | (1 << (SparkSqlParser.SHOW - 204)) | (1 << (SparkSqlParser.SKEWED - 204)) | (1 << (SparkSqlParser.SORT - 204)) | (1 << (SparkSqlParser.SORTED - 204)) | (1 << (SparkSqlParser.START - 204)) | (1 << (SparkSqlParser.STATISTICS - 204)) | (1 << (SparkSqlParser.STORED - 204)) | (1 << (SparkSqlParser.STRATIFY - 204)) | (1 << (SparkSqlParser.STRUCT - 204)) | (1 << (SparkSqlParser.SUBSTR - 204)) | (1 << (SparkSqlParser.SUBSTRING - 204)) | (1 << (SparkSqlParser.TABLES - 204)) | (1 << (SparkSqlParser.TABLESAMPLE - 204)) | (1 << (SparkSqlParser.TBLPROPERTIES - 204)) | (1 << (SparkSqlParser.TEMPORARY - 204)) | (1 << (SparkSqlParser.TERMINATED - 204)) | (1 << (SparkSqlParser.TOUCH - 204)) | (1 << (SparkSqlParser.TRANSACTION - 204)) | (1 << (SparkSqlParser.TRANSACTIONS - 204)) | (1 << (SparkSqlParser.TRANSFORM - 204)))) !== 0) || ((((_la - 236)) & ~0x1F) === 0 && ((1 << (_la - 236)) & ((1 << (SparkSqlParser.TRIM - 236)) | (1 << (SparkSqlParser.TRUE - 236)) | (1 << (SparkSqlParser.TRUNCATE - 236)) | (1 << (SparkSqlParser.TYPE - 236)) | (1 << (SparkSqlParser.UNARCHIVE - 236)) | (1 << (SparkSqlParser.UNBOUNDED - 236)) | (1 << (SparkSqlParser.UNCACHE - 236)) | (1 << (SparkSqlParser.UNLOCK - 236)) | (1 << (SparkSqlParser.UNSET - 236)) | (1 << (SparkSqlParser.UPDATE - 236)) | (1 << (SparkSqlParser.USE - 236)) | (1 << (SparkSqlParser.VALUES - 236)) | (1 << (SparkSqlParser.VIEW - 236)) | (1 << (SparkSqlParser.VIEWS - 236)) | (1 << (SparkSqlParser.WINDOW - 236)) | (1 << (SparkSqlParser.ZONE - 236)))) !== 0))) {
			this._errHandler.recoverInline(this);
			} else {
				if (this._input.LA(1) === Token.EOF) {
					this.matchedEOF = true;
				}

				this._errHandler.reportMatch(this);
				this.consume();
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public strictNonReserved(): StrictNonReservedContext {
		let _localctx: StrictNonReservedContext = new StrictNonReservedContext(this._ctx, this.state);
		this.enterRule(_localctx, 272, SparkSqlParser.RULE_strictNonReserved);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 3033;
			_la = this._input.LA(1);
			if (!(_la === SparkSqlParser.ANTI || _la === SparkSqlParser.CROSS || _la === SparkSqlParser.EXCEPT || ((((_la - 100)) & ~0x1F) === 0 && ((1 << (_la - 100)) & ((1 << (SparkSqlParser.FULL - 100)) | (1 << (SparkSqlParser.INNER - 100)) | (1 << (SparkSqlParser.INTERSECT - 100)) | (1 << (SparkSqlParser.JOIN - 100)) | (1 << (SparkSqlParser.LEFT - 100)))) !== 0) || _la === SparkSqlParser.NATURAL || _la === SparkSqlParser.ON || ((((_la - 192)) & ~0x1F) === 0 && ((1 << (_la - 192)) & ((1 << (SparkSqlParser.RIGHT - 192)) | (1 << (SparkSqlParser.SEMI - 192)) | (1 << (SparkSqlParser.SETMINUS - 192)))) !== 0) || _la === SparkSqlParser.UNION || _la === SparkSqlParser.USING)) {
			this._errHandler.recoverInline(this);
			} else {
				if (this._input.LA(1) === Token.EOF) {
					this.matchedEOF = true;
				}

				this._errHandler.reportMatch(this);
				this.consume();
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public nonReserved(): NonReservedContext {
		let _localctx: NonReservedContext = new NonReservedContext(this._ctx, this.state);
		this.enterRule(_localctx, 274, SparkSqlParser.RULE_nonReserved);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 3035;
			_la = this._input.LA(1);
			if (!(((((_la - 11)) & ~0x1F) === 0 && ((1 << (_la - 11)) & ((1 << (SparkSqlParser.ADD - 11)) | (1 << (SparkSqlParser.AFTER - 11)) | (1 << (SparkSqlParser.ALL - 11)) | (1 << (SparkSqlParser.ALTER - 11)) | (1 << (SparkSqlParser.ANALYZE - 11)) | (1 << (SparkSqlParser.AND - 11)) | (1 << (SparkSqlParser.ANY - 11)) | (1 << (SparkSqlParser.ARCHIVE - 11)) | (1 << (SparkSqlParser.ARRAY - 11)) | (1 << (SparkSqlParser.AS - 11)) | (1 << (SparkSqlParser.ASC - 11)) | (1 << (SparkSqlParser.AT - 11)) | (1 << (SparkSqlParser.AUTHORIZATION - 11)) | (1 << (SparkSqlParser.BETWEEN - 11)) | (1 << (SparkSqlParser.BOTH - 11)) | (1 << (SparkSqlParser.BUCKET - 11)) | (1 << (SparkSqlParser.BUCKETS - 11)) | (1 << (SparkSqlParser.BY - 11)) | (1 << (SparkSqlParser.CACHE - 11)) | (1 << (SparkSqlParser.CASCADE - 11)) | (1 << (SparkSqlParser.CASE - 11)) | (1 << (SparkSqlParser.CAST - 11)) | (1 << (SparkSqlParser.CHANGE - 11)) | (1 << (SparkSqlParser.CHECK - 11)) | (1 << (SparkSqlParser.CLEAR - 11)) | (1 << (SparkSqlParser.CLUSTER - 11)) | (1 << (SparkSqlParser.CLUSTERED - 11)) | (1 << (SparkSqlParser.CODEGEN - 11)) | (1 << (SparkSqlParser.COLLATE - 11)) | (1 << (SparkSqlParser.COLLECTION - 11)) | (1 << (SparkSqlParser.COLUMN - 11)))) !== 0) || ((((_la - 43)) & ~0x1F) === 0 && ((1 << (_la - 43)) & ((1 << (SparkSqlParser.COLUMNS - 43)) | (1 << (SparkSqlParser.COMMENT - 43)) | (1 << (SparkSqlParser.COMMIT - 43)) | (1 << (SparkSqlParser.COMPACT - 43)) | (1 << (SparkSqlParser.COMPACTIONS - 43)) | (1 << (SparkSqlParser.COMPUTE - 43)) | (1 << (SparkSqlParser.CONCATENATE - 43)) | (1 << (SparkSqlParser.CONSTRAINT - 43)) | (1 << (SparkSqlParser.COST - 43)) | (1 << (SparkSqlParser.CREATE - 43)) | (1 << (SparkSqlParser.CUBE - 43)) | (1 << (SparkSqlParser.CURRENT - 43)) | (1 << (SparkSqlParser.CURRENT_DATE - 43)) | (1 << (SparkSqlParser.CURRENT_TIME - 43)) | (1 << (SparkSqlParser.CURRENT_TIMESTAMP - 43)) | (1 << (SparkSqlParser.CURRENT_USER - 43)) | (1 << (SparkSqlParser.DATA - 43)) | (1 << (SparkSqlParser.DATABASE - 43)) | (1 << (SparkSqlParser.DATABASES - 43)) | (1 << (SparkSqlParser.DBPROPERTIES - 43)) | (1 << (SparkSqlParser.DEFINED - 43)) | (1 << (SparkSqlParser.DELETE - 43)) | (1 << (SparkSqlParser.DELIMITED - 43)) | (1 << (SparkSqlParser.DESC - 43)) | (1 << (SparkSqlParser.DESCRIBE - 43)) | (1 << (SparkSqlParser.DFS - 43)) | (1 << (SparkSqlParser.DIRECTORIES - 43)) | (1 << (SparkSqlParser.DIRECTORY - 43)) | (1 << (SparkSqlParser.DISTINCT - 43)) | (1 << (SparkSqlParser.DISTRIBUTE - 43)) | (1 << (SparkSqlParser.DIV - 43)))) !== 0) || ((((_la - 75)) & ~0x1F) === 0 && ((1 << (_la - 75)) & ((1 << (SparkSqlParser.DROP - 75)) | (1 << (SparkSqlParser.ELSE - 75)) | (1 << (SparkSqlParser.END - 75)) | (1 << (SparkSqlParser.ESCAPE - 75)) | (1 << (SparkSqlParser.ESCAPED - 75)) | (1 << (SparkSqlParser.EXCHANGE - 75)) | (1 << (SparkSqlParser.EXISTS - 75)) | (1 << (SparkSqlParser.EXPLAIN - 75)) | (1 << (SparkSqlParser.EXPORT - 75)) | (1 << (SparkSqlParser.EXTENDED - 75)) | (1 << (SparkSqlParser.EXTERNAL - 75)) | (1 << (SparkSqlParser.EXTRACT - 75)) | (1 << (SparkSqlParser.FALSE - 75)) | (1 << (SparkSqlParser.FETCH - 75)) | (1 << (SparkSqlParser.FIELDS - 75)) | (1 << (SparkSqlParser.FILTER - 75)) | (1 << (SparkSqlParser.FILEFORMAT - 75)) | (1 << (SparkSqlParser.FIRST - 75)) | (1 << (SparkSqlParser.FOLLOWING - 75)) | (1 << (SparkSqlParser.FOR - 75)) | (1 << (SparkSqlParser.FOREIGN - 75)) | (1 << (SparkSqlParser.FORMAT - 75)) | (1 << (SparkSqlParser.FORMATTED - 75)) | (1 << (SparkSqlParser.FROM - 75)) | (1 << (SparkSqlParser.FUNCTION - 75)) | (1 << (SparkSqlParser.FUNCTIONS - 75)) | (1 << (SparkSqlParser.GLOBAL - 75)) | (1 << (SparkSqlParser.GRANT - 75)) | (1 << (SparkSqlParser.GROUP - 75)) | (1 << (SparkSqlParser.GROUPING - 75)))) !== 0) || ((((_la - 107)) & ~0x1F) === 0 && ((1 << (_la - 107)) & ((1 << (SparkSqlParser.HAVING - 107)) | (1 << (SparkSqlParser.IF - 107)) | (1 << (SparkSqlParser.IGNORE - 107)) | (1 << (SparkSqlParser.IMPORT - 107)) | (1 << (SparkSqlParser.IN - 107)) | (1 << (SparkSqlParser.INDEX - 107)) | (1 << (SparkSqlParser.INDEXES - 107)) | (1 << (SparkSqlParser.INPATH - 107)) | (1 << (SparkSqlParser.INPUTFORMAT - 107)) | (1 << (SparkSqlParser.INSERT - 107)) | (1 << (SparkSqlParser.INTERVAL - 107)) | (1 << (SparkSqlParser.INTO - 107)) | (1 << (SparkSqlParser.IS - 107)) | (1 << (SparkSqlParser.ITEMS - 107)) | (1 << (SparkSqlParser.KEYS - 107)) | (1 << (SparkSqlParser.LAST - 107)) | (1 << (SparkSqlParser.LATERAL - 107)) | (1 << (SparkSqlParser.LAZY - 107)) | (1 << (SparkSqlParser.LEADING - 107)) | (1 << (SparkSqlParser.LIKE - 107)) | (1 << (SparkSqlParser.LIMIT - 107)) | (1 << (SparkSqlParser.LINES - 107)) | (1 << (SparkSqlParser.LIST - 107)) | (1 << (SparkSqlParser.LOAD - 107)) | (1 << (SparkSqlParser.LOCAL - 107)) | (1 << (SparkSqlParser.LOCATION - 107)) | (1 << (SparkSqlParser.LOCK - 107)) | (1 << (SparkSqlParser.LOCKS - 107)))) !== 0) || ((((_la - 139)) & ~0x1F) === 0 && ((1 << (_la - 139)) & ((1 << (SparkSqlParser.LOGICAL - 139)) | (1 << (SparkSqlParser.MACRO - 139)) | (1 << (SparkSqlParser.MAP - 139)) | (1 << (SparkSqlParser.MATCHED - 139)) | (1 << (SparkSqlParser.MERGE - 139)) | (1 << (SparkSqlParser.MSCK - 139)) | (1 << (SparkSqlParser.NAMESPACE - 139)) | (1 << (SparkSqlParser.NAMESPACES - 139)) | (1 << (SparkSqlParser.NO - 139)) | (1 << (SparkSqlParser.NOT - 139)) | (1 << (SparkSqlParser.NULL - 139)) | (1 << (SparkSqlParser.NULLS - 139)) | (1 << (SparkSqlParser.OF - 139)) | (1 << (SparkSqlParser.ONLY - 139)) | (1 << (SparkSqlParser.OPTION - 139)) | (1 << (SparkSqlParser.OPTIONS - 139)) | (1 << (SparkSqlParser.OR - 139)) | (1 << (SparkSqlParser.ORDER - 139)) | (1 << (SparkSqlParser.OUT - 139)) | (1 << (SparkSqlParser.OUTER - 139)) | (1 << (SparkSqlParser.OUTPUTFORMAT - 139)) | (1 << (SparkSqlParser.OVER - 139)) | (1 << (SparkSqlParser.OVERLAPS - 139)) | (1 << (SparkSqlParser.OVERLAY - 139)) | (1 << (SparkSqlParser.OVERWRITE - 139)) | (1 << (SparkSqlParser.PARTITION - 139)) | (1 << (SparkSqlParser.PARTITIONED - 139)) | (1 << (SparkSqlParser.PARTITIONS - 139)) | (1 << (SparkSqlParser.PERCENTLIT - 139)) | (1 << (SparkSqlParser.PIVOT - 139)))) !== 0) || ((((_la - 171)) & ~0x1F) === 0 && ((1 << (_la - 171)) & ((1 << (SparkSqlParser.PLACING - 171)) | (1 << (SparkSqlParser.POSITION - 171)) | (1 << (SparkSqlParser.PRECEDING - 171)) | (1 << (SparkSqlParser.PRIMARY - 171)) | (1 << (SparkSqlParser.PRINCIPALS - 171)) | (1 << (SparkSqlParser.PROPERTIES - 171)) | (1 << (SparkSqlParser.PURGE - 171)) | (1 << (SparkSqlParser.QUERY - 171)) | (1 << (SparkSqlParser.RANGE - 171)) | (1 << (SparkSqlParser.RECORDREADER - 171)) | (1 << (SparkSqlParser.RECORDWRITER - 171)) | (1 << (SparkSqlParser.RECOVER - 171)) | (1 << (SparkSqlParser.REDUCE - 171)) | (1 << (SparkSqlParser.REFERENCES - 171)) | (1 << (SparkSqlParser.REFRESH - 171)) | (1 << (SparkSqlParser.RENAME - 171)) | (1 << (SparkSqlParser.REPAIR - 171)) | (1 << (SparkSqlParser.REPLACE - 171)) | (1 << (SparkSqlParser.RESET - 171)) | (1 << (SparkSqlParser.RESTRICT - 171)) | (1 << (SparkSqlParser.REVOKE - 171)) | (1 << (SparkSqlParser.RLIKE - 171)) | (1 << (SparkSqlParser.ROLE - 171)) | (1 << (SparkSqlParser.ROLES - 171)) | (1 << (SparkSqlParser.ROLLBACK - 171)) | (1 << (SparkSqlParser.ROLLUP - 171)) | (1 << (SparkSqlParser.ROW - 171)) | (1 << (SparkSqlParser.ROWS - 171)) | (1 << (SparkSqlParser.SCHEMA - 171)) | (1 << (SparkSqlParser.SELECT - 171)))) !== 0) || ((((_la - 203)) & ~0x1F) === 0 && ((1 << (_la - 203)) & ((1 << (SparkSqlParser.SEPARATED - 203)) | (1 << (SparkSqlParser.SERDE - 203)) | (1 << (SparkSqlParser.SERDEPROPERTIES - 203)) | (1 << (SparkSqlParser.SESSION_USER - 203)) | (1 << (SparkSqlParser.SET - 203)) | (1 << (SparkSqlParser.SETS - 203)) | (1 << (SparkSqlParser.SHOW - 203)) | (1 << (SparkSqlParser.SKEWED - 203)) | (1 << (SparkSqlParser.SOME - 203)) | (1 << (SparkSqlParser.SORT - 203)) | (1 << (SparkSqlParser.SORTED - 203)) | (1 << (SparkSqlParser.START - 203)) | (1 << (SparkSqlParser.STATISTICS - 203)) | (1 << (SparkSqlParser.STORED - 203)) | (1 << (SparkSqlParser.STRATIFY - 203)) | (1 << (SparkSqlParser.STRUCT - 203)) | (1 << (SparkSqlParser.SUBSTR - 203)) | (1 << (SparkSqlParser.SUBSTRING - 203)) | (1 << (SparkSqlParser.TABLE - 203)) | (1 << (SparkSqlParser.TABLES - 203)) | (1 << (SparkSqlParser.TABLESAMPLE - 203)) | (1 << (SparkSqlParser.TBLPROPERTIES - 203)) | (1 << (SparkSqlParser.TEMPORARY - 203)) | (1 << (SparkSqlParser.TERMINATED - 203)) | (1 << (SparkSqlParser.THEN - 203)) | (1 << (SparkSqlParser.TIME - 203)) | (1 << (SparkSqlParser.TO - 203)) | (1 << (SparkSqlParser.TOUCH - 203)) | (1 << (SparkSqlParser.TRAILING - 203)) | (1 << (SparkSqlParser.TRANSACTION - 203)) | (1 << (SparkSqlParser.TRANSACTIONS - 203)))) !== 0) || ((((_la - 235)) & ~0x1F) === 0 && ((1 << (_la - 235)) & ((1 << (SparkSqlParser.TRANSFORM - 235)) | (1 << (SparkSqlParser.TRIM - 235)) | (1 << (SparkSqlParser.TRUE - 235)) | (1 << (SparkSqlParser.TRUNCATE - 235)) | (1 << (SparkSqlParser.TYPE - 235)) | (1 << (SparkSqlParser.UNARCHIVE - 235)) | (1 << (SparkSqlParser.UNBOUNDED - 235)) | (1 << (SparkSqlParser.UNCACHE - 235)) | (1 << (SparkSqlParser.UNIQUE - 235)) | (1 << (SparkSqlParser.UNKNOWN - 235)) | (1 << (SparkSqlParser.UNLOCK - 235)) | (1 << (SparkSqlParser.UNSET - 235)) | (1 << (SparkSqlParser.UPDATE - 235)) | (1 << (SparkSqlParser.USE - 235)) | (1 << (SparkSqlParser.USER - 235)) | (1 << (SparkSqlParser.VALUES - 235)) | (1 << (SparkSqlParser.VIEW - 235)) | (1 << (SparkSqlParser.VIEWS - 235)) | (1 << (SparkSqlParser.WHEN - 235)) | (1 << (SparkSqlParser.WHERE - 235)) | (1 << (SparkSqlParser.WINDOW - 235)) | (1 << (SparkSqlParser.WITH - 235)) | (1 << (SparkSqlParser.ZONE - 235)))) !== 0))) {
			this._errHandler.recoverInline(this);
			} else {
				if (this._input.LA(1) === Token.EOF) {
					this.matchedEOF = true;
				}

				this._errHandler.reportMatch(this);
				this.consume();
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}

	public sempred(_localctx: RuleContext, ruleIndex: number, predIndex: number): boolean {
		switch (ruleIndex) {
		case 42:
			return this.queryTerm_sempred(_localctx as QueryTermContext, predIndex);

		case 96:
			return this.booleanExpression_sempred(_localctx as BooleanExpressionContext, predIndex);

		case 98:
			return this.valueExpression_sempred(_localctx as ValueExpressionContext, predIndex);

		case 99:
			return this.primaryExpression_sempred(_localctx as PrimaryExpressionContext, predIndex);

		case 130:
			return this.identifier_sempred(_localctx as IdentifierContext, predIndex);

		case 131:
			return this.strictIdentifier_sempred(_localctx as StrictIdentifierContext, predIndex);

		case 133:
			return this.number_sempred(_localctx as NumberContext, predIndex);
		}
		return true;
	}
	private queryTerm_sempred(_localctx: QueryTermContext, predIndex: number): boolean {
		switch (predIndex) {
		case 0:
			return this.precpred(this._ctx, 3);

		case 1:
			return legacy_setops_precedence_enbled;

		case 2:
			return this.precpred(this._ctx, 2);

		case 3:
			return !legacy_setops_precedence_enbled;

		case 4:
			return this.precpred(this._ctx, 1);

		case 5:
			return !legacy_setops_precedence_enbled;
		}
		return true;
	}
	private booleanExpression_sempred(_localctx: BooleanExpressionContext, predIndex: number): boolean {
		switch (predIndex) {
		case 6:
			return this.precpred(this._ctx, 2);

		case 7:
			return this.precpred(this._ctx, 1);
		}
		return true;
	}
	private valueExpression_sempred(_localctx: ValueExpressionContext, predIndex: number): boolean {
		switch (predIndex) {
		case 8:
			return this.precpred(this._ctx, 6);

		case 9:
			return this.precpred(this._ctx, 5);

		case 10:
			return this.precpred(this._ctx, 4);

		case 11:
			return this.precpred(this._ctx, 3);

		case 12:
			return this.precpred(this._ctx, 2);

		case 13:
			return this.precpred(this._ctx, 1);
		}
		return true;
	}
	private primaryExpression_sempred(_localctx: PrimaryExpressionContext, predIndex: number): boolean {
		switch (predIndex) {
		case 14:
			return this.precpred(this._ctx, 8);

		case 15:
			return this.precpred(this._ctx, 6);
		}
		return true;
	}
	private identifier_sempred(_localctx: IdentifierContext, predIndex: number): boolean {
		switch (predIndex) {
		case 16:
			return !SQL_standard_keyword_behavior;
		}
		return true;
	}
	private strictIdentifier_sempred(_localctx: StrictIdentifierContext, predIndex: number): boolean {
		switch (predIndex) {
		case 17:
			return SQL_standard_keyword_behavior;

		case 18:
			return !SQL_standard_keyword_behavior;
		}
		return true;
	}
	private number_sempred(_localctx: NumberContext, predIndex: number): boolean {
		switch (predIndex) {
		case 19:
			return !legacy_exponent_literal_as_decimal_enabled;

		case 20:
			return !legacy_exponent_literal_as_decimal_enabled;

		case 21:
			return legacy_exponent_literal_as_decimal_enabled;
		}
		return true;
	}

	private static readonly _serializedATNSegments: number = 6;
	private static readonly _serializedATNSegment0: string =
		"\x03\uC91D\uCABA\u058D\uAFBA\u4F53\u0607\uEA8B\uC241\x03\u0128\u0BE0\x04" +
		"\x02\t\x02\x04\x03\t\x03\x04\x04\t\x04\x04\x05\t\x05\x04\x06\t\x06\x04" +
		"\x07\t\x07\x04\b\t\b\x04\t\t\t\x04\n\t\n\x04\v\t\v\x04\f\t\f\x04\r\t\r" +
		"\x04\x0E\t\x0E\x04\x0F\t\x0F\x04\x10\t\x10\x04\x11\t\x11\x04\x12\t\x12" +
		"\x04\x13\t\x13\x04\x14\t\x14\x04\x15\t\x15\x04\x16\t\x16\x04\x17\t\x17" +
		"\x04\x18\t\x18\x04\x19\t\x19\x04\x1A\t\x1A\x04\x1B\t\x1B\x04\x1C\t\x1C" +
		"\x04\x1D\t\x1D\x04\x1E\t\x1E\x04\x1F\t\x1F\x04 \t \x04!\t!\x04\"\t\"\x04" +
		"#\t#\x04$\t$\x04%\t%\x04&\t&\x04\'\t\'\x04(\t(\x04)\t)\x04*\t*\x04+\t" +
		"+\x04,\t,\x04-\t-\x04.\t.\x04/\t/\x040\t0\x041\t1\x042\t2\x043\t3\x04" +
		"4\t4\x045\t5\x046\t6\x047\t7\x048\t8\x049\t9\x04:\t:\x04;\t;\x04<\t<\x04" +
		"=\t=\x04>\t>\x04?\t?\x04@\t@\x04A\tA\x04B\tB\x04C\tC\x04D\tD\x04E\tE\x04" +
		"F\tF\x04G\tG\x04H\tH\x04I\tI\x04J\tJ\x04K\tK\x04L\tL\x04M\tM\x04N\tN\x04" +
		"O\tO\x04P\tP\x04Q\tQ\x04R\tR\x04S\tS\x04T\tT\x04U\tU\x04V\tV\x04W\tW\x04" +
		"X\tX\x04Y\tY\x04Z\tZ\x04[\t[\x04\\\t\\\x04]\t]\x04^\t^\x04_\t_\x04`\t" +
		"`\x04a\ta\x04b\tb\x04c\tc\x04d\td\x04e\te\x04f\tf\x04g\tg\x04h\th\x04" +
		"i\ti\x04j\tj\x04k\tk\x04l\tl\x04m\tm\x04n\tn\x04o\to\x04p\tp\x04q\tq\x04" +
		"r\tr\x04s\ts\x04t\tt\x04u\tu\x04v\tv\x04w\tw\x04x\tx\x04y\ty\x04z\tz\x04" +
		"{\t{\x04|\t|\x04}\t}\x04~\t~\x04\x7F\t\x7F\x04\x80\t\x80\x04\x81\t\x81" +
		"\x04\x82\t\x82\x04\x83\t\x83\x04\x84\t\x84\x04\x85\t\x85\x04\x86\t\x86" +
		"\x04\x87\t\x87\x04\x88\t\x88\x04\x89\t\x89\x04\x8A\t\x8A\x04\x8B\t\x8B" +
		"\x03\x02\x03\x02\x03\x02\x03\x03\x03\x03\x05\x03\u011C\n\x03\x03\x03\x07" +
		"\x03\u011F\n\x03\f\x03\x0E\x03\u0122\v\x03\x03\x04\x03\x04\x03\x05\x03" +
		"\x05\x03\x05\x03\x06\x03\x06\x03\x06\x03\x07\x03\x07\x03\x07\x03\b\x03" +
		"\b\x03\b\x03\t\x03\t\x03\t\x03\n\x03\n\x05\n\u0137\n\n\x03\n\x03\n\x03" +
		"\n\x05\n\u013C\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x05\n\u0144\n\n" +
		"\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x07\n\u014C\n\n\f\n\x0E\n\u014F\v" +
		"\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03" +
		"\n\x03\n\x03\n\x03\n\x03\n\x03\n\x05\n\u0162\n\n\x03\n\x03\n\x05\n\u0166" +
		"\n\n\x03\n\x03\n\x03\n\x03\n\x05\n\u016C\n\n\x03\n\x05\n\u016F\n\n\x03" +
		"\n\x05\n\u0172\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x05\n\u0179\n\n\x03\n" +
		"\x03\n\x03\n\x05\n\u017E\n\n\x03\n\x05\n\u0181\n\n\x03\n\x03\n\x03\n\x03" +
		"\n\x03\n\x05\n\u0188\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03" +
		"\n\x03\n\x03\n\x05\n\u0194\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03" +
		"\n\x07\n\u019D\n\n\f\n\x0E\n\u01A0\v\n\x03\n\x05\n\u01A3\n\n\x03\n\x05" +
		"\n\u01A6\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x05\n\u01AD\n\n\x03\n\x03\n" +
		"\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x07\n\u01B8\n\n\f\n\x0E\n\u01BB" +
		"\v\n\x03\n\x03\n\x03\n\x03\n\x03\n\x05\n\u01C2\n\n\x03\n\x03\n\x03\n\x05" +
		"\n\u01C7\n\n\x03\n\x05\n\u01CA\n\n\x03\n\x03\n\x03\n\x03\n\x05\n\u01D0" +
		"\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x05\n\u01DB" +
		"\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n" +
		"\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03" +
		"\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03" +
		"\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03" +
		"\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03" +
		"\n\x03\n\x03\n\x03\n\x05\n\u021B\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03" +
		"\n\x03\n\x05\n\u0224\n\n\x03\n\x03\n\x05\n\u0228\n\n\x03\n\x03\n\x03\n" +
		"\x03\n\x05\n\u022E\n\n\x03\n\x03\n\x05\n\u0232\n\n\x03\n\x03\n\x03\n\x05" +
		"\n\u0237\n\n\x03\n\x03\n\x03\n\x03\n\x05\n\u023D\n\n\x03\n\x03\n\x03\n" +
		"\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x05\n\u0249\n\n\x03\n\x03\n" +
		"\x03\n\x03\n\x03\n\x03\n\x05\n\u0251\n\n\x03\n\x03\n\x03\n\x03\n\x05\n" +
		"\u0257\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n" +
		"\x03\n\x05\n\u0264\n\n\x03\n\x06\n\u0267\n\n\r\n\x0E\n\u0268\x03\n\x03" +
		"\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03" +
		"\n\x05\n\u0279\n\n\x03\n\x03\n\x03\n\x07\n\u027E\n\n\f\n\x0E\n\u0281\v" +
		"\n\x03\n\x05\n\u0284\n\n\x03\n\x03\n\x03\n\x03\n\x05\n\u028A\n\n\x03\n" +
		"\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03" +
		"\n\x05\n\u0299\n\n\x03\n\x03\n\x05\n\u029D\n\n\x03\n\x03\n\x03\n\x03\n" +
		"\x05\n\u02A3\n\n\x03\n\x03\n\x03\n\x03\n\x05\n\u02A9\n\n\x03\n\x05\n\u02AC" +
		"\n\n\x03\n\x05\n\u02AF\n\n\x03\n\x03\n\x03\n\x03\n\x05\n\u02B5\n\n\x03" +
		"\n\x03\n\x05\n\u02B9\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x07\n\u02C1" +
		"\n\n\f\n\x0E\n\u02C4\v\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x05\n\u02CC" +
		"\n\n\x03\n\x05\n\u02CF\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x05" +
		"\n\u02D8\n\n\x03\n\x03\n\x03\n\x05\n\u02DD\n\n\x03\n\x03\n\x03\n\x03\n" +
		"\x05\n\u02E3\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x05\n\u02EA\n\n\x03\n\x05" +
		"\n\u02ED\n\n\x03\n\x03\n\x03\n\x03\n\x05\n\u02F3\n\n\x03\n\x03\n\x03\n" +
		"\x03\n\x03\n\x03\n\x03\n\x07\n\u02FC\n\n\f\n\x0E\n\u02FF\v\n\x05\n\u0301" +
		"\n\n\x03\n\x03\n\x05\n\u0305\n\n\x03\n\x03\n\x03\n\x05\n\u030A\n\n\x03" +
		"\n\x03\n\x03\n\x05\n\u030F\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x05\n\u0316" +
		"\n\n\x03\n\x05\n\u0319\n\n\x03\n\x05\n\u031C\n\n\x03\n\x03\n\x03\n\x03" +
		"\n\x03\n\x05\n\u0323\n\n\x03\n\x03\n\x03\n\x05\n\u0328\n\n\x03\n\x03\n" +
		"\x03\n\x03\n\x03\n\x03\n\x03\n\x05\n\u0331\n\n\x03\n\x03\n\x03\n\x03\n" +
		"\x03\n\x03\n\x05\n\u0339\n\n\x03\n\x03\n\x03\n\x03\n\x05\n\u033F\n\n\x03" +
		"\n\x05\n\u0342\n\n\x03\n\x05\n\u0345\n\n\x03\n\x03\n\x03\n\x03\n\x05\n" +
		"\u034B\n\n\x03\n\x03\n\x05\n\u034F\n\n\x03\n\x03\n\x05\n\u0353\n\n\x03" +
		"\n\x03\n\x05\n\u0357\n\n\x05\n\u0359\n\n\x03\n\x03\n\x03\n\x03\n\x03\n" +
		"\x03\n\x05\n\u0361\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x05\n\u0369" +
		"\n\n\x03\n\x03\n\x03\n\x03\n\x05\n\u036F\n\n\x03\n\x03\n\x03\n\x03\n\x05" +
		"\n\u0375\n\n\x03\n\x05\n\u0378\n\n\x03\n\x03\n\x05\n\u037C\n\n\x03\n\x05" +
		"\n\u037F\n\n\x03\n\x03\n\x05\n\u0383\n\n\x03\n\x03\n\x03\n\x03\n\x03\n" +
		"\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03" +
		"\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x07\n\u039D\n\n\f\n\x0E\n" +
		"\u03A0\v\n\x05\n\u03A2\n\n\x03\n\x03\n\x05\n\u03A6\n\n\x03\n\x03\n\x03" +
		"\n\x03\n\x05\n\u03AC\n\n\x03\n\x05\n\u03AF\n\n\x03\n\x05\n\u03B2\n\n\x03" +
		"\n\x03\n\x03\n\x03\n\x05\n\u03B8\n\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03" +
		"\n\x05\n\u03C0\n\n\x03\n\x03\n\x03\n\x05\n\u03C5\n\n\x03\n\x03\n\x03\n" +
		"\x03\n\x05\n\u03CB\n\n\x03\n\x03\n\x03\n\x03\n\x05\n\u03D1\n\n\x03\n\x03" +
		"\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x07\n\u03DB\n\n\f\n\x0E\n\u03DE" +
		"\v\n\x05\n\u03E0\n\n\x03\n\x03\n\x03\n\x07\n\u03E5\n\n\f\n\x0E\n\u03E8" +
		"\v\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n" +
		"\x03\n\x07\n\u03F6\n\n\f\n\x0E\n\u03F9\v\n\x03\n\x03\n\x03\n\x03\n\x07" +
		"\n\u03FF\n\n\f\n\x0E\n\u0402\v\n\x05\n\u0404\n\n\x03\n\x03\n\x07\n\u0408" +
		"\n\n\f\n\x0E\n\u040B\v\n\x03\n\x03\n\x03\n\x03\n\x07\n\u0411\n\n\f\n\x0E" +
		"\n\u0414\v\n\x03\n\x03\n\x07\n\u0418\n\n\f\n\x0E\n\u041B\v\n\x05\n\u041D" +
		"\n\n\x03\v\x03\v\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x05\f\u0427\n\f\x03" +
		"\f\x03\f\x05\f\u042B\n\f\x03\f\x03\f\x03\f\x03\f\x03\f\x05\f\u0432\n\f" +
		"\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03" +
		"\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03" +
		"\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03" +
		"\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03" +
		"\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03" +
		"\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03" +
		"\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03" +
		"\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03" +
		"\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03" +
		"\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x05\f\u04A6\n\f\x03\f\x03\f\x03" +
		"\f\x03\f\x03\f\x03\f\x05\f\u04AE\n\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03" +
		"\f\x05\f\u04B6\n\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x05\f\u04BF" +
		"\n\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x03\f\x05\f\u04C9\n\f\x03" +
		"\r\x03\r\x05\r\u04CD\n\r\x03\r\x05\r\u04D0\n\r\x03\r\x03\r\x03\r\x03\r" +
		"\x05\r\u04D6\n\r\x03\r\x03\r\x03\x0E\x03\x0E\x05\x0E\u04DC\n\x0E\x03\x0E" +
		"\x03\x0E\x03\x0E\x03\x0E\x03\x0F\x03\x0F\x03\x0F\x03\x0F\x03\x0F\x03\x0F" +
		"\x05\x0F\u04E8\n\x0F\x03\x0F\x03\x0F\x03\x0F\x03\x0F\x03\x10\x03\x10\x03" +
		"\x10\x03\x10\x03\x10\x03\x10\x05\x10\u04F4\n\x10\x03\x10\x03\x10\x03\x10" +
		"\x05\x10\u04F9\n\x10\x03\x11\x03\x11\x03\x11\x03\x12\x03\x12\x03\x12\x03" +
		"\x13\x05\x13\u0502\n\x13\x03\x13\x03\x13\x03\x13\x03\x14\x03\x14\x03\x14" +
		"\x05\x14\u050A\n\x14\x03\x14\x03\x14\x03\x14\x03\x14\x03\x14\x05\x14\u0511" +
		"\n\x14\x05\x14\u0513\n\x14\x03\x14\x03\x14\x03\x14\x05\x14\u0518\n\x14" +
		"\x03\x14\x03\x14\x05\x14\u051C\n\x14\x03\x14\x03\x14\x03\x14\x05\x14\u0521" +
		"\n\x14\x03\x14\x03\x14\x03\x14\x05\x14\u0526\n\x14\x03\x14\x03\x14\x03" +
		"\x14\x05\x14\u052B\n\x14\x03\x14\x05\x14\u052E\n\x14\x03\x14\x03\x14\x03" +
		"\x14\x05\x14\u0533\n\x14\x03\x14\x03\x14\x05\x14\u0537\n\x14\x03\x14\x03" +
		"\x14\x03\x14\x05\x14\u053C\n\x14\x05\x14\u053E\n\x14\x03\x15\x03\x15\x05" +
		"\x15\u0542\n\x15\x03\x16\x03\x16\x03\x16\x03\x16\x03\x16\x07\x16\u0549" +
		"\n\x16\f\x16\x0E\x16\u054C\v\x16\x03\x16\x03\x16\x03\x17\x03\x17\x03\x17" +
		"\x05\x17\u0553\n\x17\x03\x18\x03\x18\x03\x19\x03\x19\x03\x19\x03\x19\x03" +
		"\x19\x05\x19\u055C\n\x19\x03\x1A\x03\x1A\x03\x1A\x07\x1A\u0561\n\x1A\f" +
		"\x1A\x0E\x1A\u0564\v\x1A\x03\x1B\x03\x1B\x03\x1B\x03\x1B\x07\x1B\u056A" +
		"\n\x1B\f\x1B\x0E\x1B\u056D\v\x1B\x03\x1C\x03\x1C\x05\x1C\u0571\n\x1C\x03" +
		"\x1C\x05\x1C\u0574\n\x1C\x03\x1C\x03\x1C\x03\x1C\x03\x1C\x03\x1D\x03\x1D" +
		"\x03\x1D\x03\x1E\x03\x1E\x03\x1E\x03\x1E\x03\x1E\x03\x1E\x03\x1E\x03\x1E" +
		"\x03\x1E\x03\x1E\x07\x1E\u0587\n\x1E\f\x1E\x0E\x1E\u058A\v\x1E\x03\x1F" +
		"\x03\x1F\x03\x1F\x03\x1F\x07\x1F\u0590\n\x1F\f\x1F\x0E\x1F\u0593\v\x1F" +
		"\x03\x1F\x03\x1F\x03 \x03 \x05 \u0599\n \x03 \x05 \u059C\n \x03!\x03!" +
		"\x03!\x07!\u05A1\n!\f!\x0E!\u05A4\v!\x03!\x05!\u05A7\n!\x03\"\x03\"\x03" +
		"\"\x03\"\x05\"\u05AD\n\"\x03#\x03#\x03#\x03#\x07#\u05B3\n#\f#\x0E#\u05B6" +
		"\v#\x03#\x03#\x03$\x03$\x03$\x03$\x07$\u05BE\n$\f$\x0E$\u05C1\v$\x03$" +
		"\x03$\x03%\x03%\x03%\x03%\x03%\x03%\x05%\u05CB\n%\x03&\x03&\x03&\x03&" +
		"\x03&\x05&\u05D2\n&\x03\'\x03\'\x03\'\x03\'\x05\'\u05D8\n\'\x03(\x03(" +
		"\x03(\x03)\x03)\x03)\x03)\x03)\x03)\x06)\u05E3\n)\r)\x0E)\u05E4\x03)\x03" +
		")\x03)\x03)\x03)\x05)\u05EC\n)\x03)\x03)\x03)\x03)\x03)\x05)\u05F3\n)" +
		"\x03)\x03)\x03)\x03)\x03)\x03)\x03)\x03)\x03)\x03)\x05)\u05FF\n)\x03)" +
		"\x03)\x03)\x03)\x07)\u0605\n)\f)\x0E)\u0608\v)\x03)\x07)\u060B\n)\f)\x0E" +
		")\u060E\v)\x05)\u0610\n)\x03*\x03*\x03*\x03*\x03*\x07*\u0617\n*\f*\x0E" +
		"*\u061A\v*\x05*\u061C\n*\x03*\x03*\x03*\x03*\x03*\x07*\u0623\n*\f*\x0E" +
		"*\u0626\v*\x05*\u0628\n*\x03*\x03*\x03*\x03*\x03*\x07*\u062F\n*\f*\x0E" +
		"*\u0632\v*\x05*\u0634\n*\x03*\x03*\x03*\x03*\x03*\x07*\u063B\n*\f*\x0E" +
		"*\u063E\v*\x05*\u0640\n*\x03*\x05*\u0643\n*\x03*\x03*\x03*\x05*\u0648" +
		"\n*\x05*\u064A\n*\x03+\x03+\x03+\x03,\x03,\x03,\x03,\x03,\x03,\x03,\x05" +
		",\u0656\n,\x03,\x03,\x03,\x03,\x03,\x05,\u065D\n,\x03,\x03,\x03,\x03," +
		"\x03,\x05,\u0664\n,\x03,\x07,\u0667\n,\f,\x0E,\u066A\v,\x03-\x03-\x03" +
		"-\x03-\x03-\x03-\x03-\x03-\x03-\x05-\u0675\n-\x03.\x03.\x05.\u0679\n." +
		"\x03.\x03.\x05.\u067D\n.\x03/\x03/\x06/\u0681\n/\r/\x0E/\u0682\x030\x03" +
		"0\x050\u0687\n0\x030\x030\x030\x030\x070\u068D\n0\f0\x0E0\u0690\v0\x03" +
		"0\x050\u0693\n0\x030\x050\u0696\n0\x030\x050\u0699\n0\x030\x050\u069C" +
		"\n0\x030\x030\x050\u06A0\n0\x031\x031\x051\u06A4\n1\x031\x051\u06A7\n" +
		"1\x031\x031\x051\u06AB\n1\x031\x071\u06AE\n1\f1\x0E1\u06B1\v1\x031\x05" +
		"1\u06B4\n1\x031\x051\u06B7\n1\x031\x051\u06BA\n1\x031\x051\u06BD\n1\x05" +
		"1\u06BF\n1\x032\x032\x032\x032\x032\x032\x032\x032\x032\x032\x052\u06CB" +
		"\n2\x032\x052\u06CE\n2\x032\x032\x052\u06D2\n2\x032\x032\x032\x032\x03" +
		"2\x032\x032\x032\x052\u06DC\n2\x032\x032\x052\u06E0\n2\x052\u06E2\n2\x03" +
		"2\x052\u06E5\n2\x032\x032\x052\u06E9\n2\x033\x033\x073\u06ED\n3\f3\x0E" +
		"3\u06F0\v3\x033\x053\u06F3\n3\x033\x033\x034\x034\x034\x035\x035\x035" +
		"\x035\x055\u06FE\n5\x035\x035\x035\x036\x036\x036\x036\x036\x056\u0708" +
		"\n6\x036\x036\x036\x037\x037\x037\x037\x037\x037\x037\x057\u0714\n7\x03" +
		"8\x038\x038\x038\x038\x038\x038\x038\x038\x038\x038\x078\u0721\n8\f8\x0E" +
		"8\u0724\v8\x038\x038\x058\u0728\n8\x039\x039\x039\x079\u072D\n9\f9\x0E" +
		"9\u0730\v9\x03:\x03:\x03:\x03:\x03;\x03;\x03;\x03<\x03<\x03<\x03=\x03" +
		"=\x03=\x05=\u073F\n=\x03=\x07=\u0742\n=\f=\x0E=\u0745\v=\x03=\x03=\x03" +
		">\x03>\x03>\x03>\x03>\x03>\x07>\u074F\n>\f>\x0E>\u0752\v>\x03>\x03>\x05" +
		">\u0756\n>\x03?\x03?\x03?\x03?\x03?\x03?\x03?\x07?\u075F\n?\f?\x0E?\u0762" +
		"\v?\x03?\x07?\u0765\n?\f?\x0E?\u0768\v?\x03?\x05?\u076B\n?\x03@\x03@\x03" +
		"A\x03A\x03A\x03A\x03A\x07A\u0774\nA\fA\x0EA\u0777\vA\x03A\x03A\x03A\x03" +
		"A\x03A\x03A\x03A\x03A\x03A\x03A\x07A\u0783\nA\fA\x0EA\u0786\vA\x03A\x03" +
		"A\x05A\u078A\nA\x03A\x03A\x03A\x03A\x03A\x03A\x03A\x03A\x07A\u0794\nA" +
		"\fA\x0EA\u0797\vA\x03A\x03A\x05A\u079B\nA\x03B\x03B\x03B\x03B\x07B\u07A1" +
		"\nB\fB\x0EB\u07A4\vB\x05B\u07A6\nB\x03B\x03B\x05B\u07AA\nB\x03C\x03C\x03" +
		"C\x03C\x03C\x03C\x03C\x03C\x03C\x03C\x07C\u07B6\nC\fC\x0EC\u07B9\vC\x03" +
		"C\x03C\x03C\x03D\x03D\x03D\x03D\x03D\x07D\u07C3\nD\fD\x0ED\u07C6\vD\x03" +
		"D\x03D\x05D\u07CA\nD\x03E\x03E\x05E\u07CE\nE\x03E\x05E\u07D1\nE\x03F\x03" +
		"F\x03F\x05F\u07D6\nF\x03F\x03F\x03F\x03F\x03F\x07F\u07DD\nF\fF\x0EF\u07E0" +
		"\vF\x05F\u07E2\nF\x03F\x03F\x03F\x05F\u07E7\nF\x03F\x03F\x03F\x07F\u07EC" +
		"\nF\fF\x0EF\u07EF\vF\x05F\u07F1\nF\x03G\x03G\x03H\x03H\x07H\u07F7\nH\f" +
		"H\x0EH\u07FA\vH\x03I\x03I\x03I\x03I\x05I\u0800\nI\x03I\x03I\x03I\x03I" +
		"\x03I\x05I\u0807\nI\x03J\x05J\u080A\nJ\x03J\x03J\x03J\x05J\u080F\nJ\x03" +
		"J\x05J\u0812\nJ\x03J\x03J\x03J\x05J\u0817\nJ\x03J\x03J\x05J\u081B\nJ\x03" +
		"J\x05J\u081E\nJ\x03J\x05J\u0821\nJ\x03K\x03K\x03K\x03K\x05K\u0827\nK\x03" +
		"L\x03L\x03L\x05L\u082C\nL\x03L\x03L\x03M\x05M\u0831\nM\x03M\x03M\x03M" +
		"\x03M\x03M\x03M\x03M\x03M\x03M\x03M\x03M\x03M\x03M\x03M\x03M\x03M\x05" +
		"M\u0843\nM\x05M\u0845\nM\x03M\x05M\u0848\nM\x03N\x03N\x03N\x03N\x03O\x03" +
		"O\x03O\x07O\u0851\nO\fO\x0EO\u0854\vO\x03P\x03P\x03P\x03P\x07P\u085A\n" +
		"P\fP\x0EP\u085D\vP\x03P\x03P\x03Q\x03Q\x05Q\u0863\nQ\x03R\x03R\x03R\x03" +
		"R\x07R\u0869\nR\fR\x0ER\u086C\vR\x03R\x03R\x03S\x03S\x05S\u0872\nS\x03" +
		"T\x03T\x05T\u0876\nT\x03T\x03T\x03T\x03T\x03T\x03T\x05T\u087E\nT\x03T" +
		"\x03T\x03T\x03T\x03T\x03T\x05T\u0886\nT\x03T\x03T\x03T\x03T\x05T\u088C" +
		"\nT\x03U\x03U\x03U\x03U\x07U\u0892\nU\fU\x0EU\u0895\vU\x03U\x03U\x03V" +
		"\x03V\x03V\x03V\x03V\x07V\u089E\nV\fV\x0EV\u08A1\vV\x05V\u08A3\nV\x03" +
		"V\x03V\x03V\x03W\x05W\u08A9\nW\x03W\x03W\x05W\u08AD\nW\x05W\u08AF\nW\x03" +
		"X\x03X\x03X\x03X\x03X\x03X\x03X\x05X\u08B8\nX\x03X\x03X\x03X\x03X\x03" +
		"X\x03X\x03X\x03X\x03X\x03X\x05X\u08C4\nX\x05X\u08C6\nX\x03X\x03X\x03X" +
		"\x03X\x03X\x05X\u08CD\nX\x03X\x03X\x03X\x03X\x03X\x05X\u08D4\nX\x03X\x03" +
		"X\x03X\x03X\x05X\u08DA\nX\x03X\x03X\x03X\x03X\x05X\u08E0\nX\x05X\u08E2" +
		"\nX\x03Y\x03Y\x03Y\x07Y\u08E7\nY\fY\x0EY\u08EA\vY\x03Z\x03Z\x03Z\x07Z" +
		"\u08EF\nZ\fZ\x0EZ\u08F2\vZ\x03[\x03[\x03[\x05[\u08F7\n[\x03[\x03[\x03" +
		"\\\x03\\\x05\\\u08FD\n\\\x03\\\x03\\\x05\\\u0901\n\\\x05\\\u0903\n\\\x03" +
		"]\x03]\x03]\x07]\u0908\n]\f]\x0E]\u090B\v]\x03^\x03^\x03^\x03^\x07^\u0911" +
		"\n^\f";
	private static readonly _serializedATNSegment1: string =
		"^\x0E^\u0914\v^\x03^\x03^\x03_\x03_\x03_\x03_\x03_\x03_\x07_\u091E\n_" +
		"\f_\x0E_\u0921\v_\x03_\x03_\x05_\u0925\n_\x03`\x03`\x05`\u0929\n`\x03" +
		"a\x03a\x03b\x03b\x03b\x03b\x03b\x03b\x03b\x03b\x03b\x03b\x05b\u0937\n" +
		"b\x05b\u0939\nb\x03b\x03b\x03b\x03b\x03b\x03b\x07b\u0941\nb\fb\x0Eb\u0944" +
		"\vb\x03c\x05c\u0947\nc\x03c\x03c\x03c\x03c\x03c\x03c\x05c\u094F\nc\x03" +
		"c\x03c\x03c\x03c\x03c\x07c\u0956\nc\fc\x0Ec\u0959\vc\x03c\x03c\x03c\x05" +
		"c\u095E\nc\x03c\x03c\x03c\x03c\x03c\x03c\x05c\u0966\nc\x03c\x03c\x03c" +
		"\x05c\u096B\nc\x03c\x03c\x03c\x03c\x03c\x03c\x03c\x03c\x07c\u0975\nc\f" +
		"c\x0Ec\u0978\vc\x03c\x03c\x05c\u097C\nc\x03c\x05c\u097F\nc\x03c\x03c\x03" +
		"c\x03c\x05c\u0985\nc\x03c\x03c\x05c\u0989\nc\x03c\x03c\x03c\x05c\u098E" +
		"\nc\x03c\x03c\x03c\x05c\u0993\nc\x03c\x03c\x03c\x05c\u0998\nc\x03d\x03" +
		"d\x03d\x03d\x05d\u099E\nd\x03d\x03d\x03d\x03d\x03d\x03d\x03d\x03d\x03" +
		"d\x03d\x03d\x03d\x03d\x03d\x03d\x03d\x03d\x03d\x03d\x07d\u09B3\nd\fd\x0E" +
		"d\u09B6\vd\x03e\x03e\x03e\x03e\x06e\u09BC\ne\re\x0Ee\u09BD\x03e\x03e\x05" +
		"e\u09C2\ne\x03e\x03e\x03e\x03e\x03e\x06e\u09C9\ne\re\x0Ee\u09CA\x03e\x03" +
		"e\x05e\u09CF\ne\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03" +
		"e\x03e\x03e\x03e\x07e\u09DF\ne\fe\x0Ee\u09E2\ve\x05e\u09E4\ne\x03e\x03" +
		"e\x03e\x03e\x03e\x03e\x05e\u09EC\ne\x03e\x03e\x03e\x03e\x03e\x03e\x03" +
		"e\x05e\u09F5\ne\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03" +
		"e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x06e\u0A0A\ne\re\x0Ee\u0A0B" +
		"\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x05e\u0A17\ne\x03e\x03e" +
		"\x03e\x07e\u0A1C\ne\fe\x0Ee\u0A1F\ve\x05e\u0A21\ne\x03e\x03e\x03e\x03" +
		"e\x03e\x03e\x03e\x05e\u0A2A\ne\x03e\x03e\x05e\u0A2E\ne\x03e\x03e\x03e" +
		"\x03e\x03e\x03e\x03e\x03e\x06e\u0A38\ne\re\x0Ee\u0A39\x03e\x03e\x03e\x03" +
		"e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03" +
		"e\x03e\x03e\x03e\x03e\x03e\x05e\u0A53\ne\x03e\x03e\x03e\x03e\x03e\x05" +
		"e\u0A5A\ne\x03e\x05e\u0A5D\ne\x03e\x03e\x03e\x03e\x03e\x03e\x03e\x03e" +
		"\x03e\x03e\x03e\x03e\x03e\x05e\u0A6C\ne\x03e\x03e\x05e\u0A70\ne\x03e\x03" +
		"e\x03e\x03e\x03e\x03e\x03e\x03e\x07e\u0A7A\ne\fe\x0Ee\u0A7D\ve\x03f\x03" +
		"f\x03f\x03f\x03f\x03f\x03f\x03f\x06f\u0A87\nf\rf\x0Ef\u0A88\x05f\u0A8B" +
		"\nf\x03g\x03g\x03h\x03h\x03i\x03i\x03j\x03j\x03k\x03k\x03k\x05k\u0A98" +
		"\nk\x03l\x03l\x05l\u0A9C\nl\x03m\x03m\x03m\x06m\u0AA1\nm\rm\x0Em\u0AA2" +
		"\x03n\x03n\x03n\x05n\u0AA8\nn\x03o\x03o\x03o\x03o\x03o\x03p\x05p\u0AB0" +
		"\np\x03p\x03p\x05p\u0AB4\np\x03q\x03q\x03q\x05q\u0AB9\nq\x03r\x03r\x03" +
		"r\x03r\x03r\x03r\x03r\x03r\x03r\x03r\x03r\x03r\x03r\x03r\x03r\x05r\u0ACA" +
		"\nr\x03r\x03r\x05r\u0ACE\nr\x03r\x03r\x03r\x03r\x03r\x07r\u0AD5\nr\fr" +
		"\x0Er\u0AD8\vr\x03r\x05r\u0ADB\nr\x05r\u0ADD\nr\x03s\x03s\x03s\x07s\u0AE2" +
		"\ns\fs\x0Es\u0AE5\vs\x03t\x03t\x03t\x03t\x05t\u0AEB\nt\x03t\x05t\u0AEE" +
		"\nt\x03t\x05t\u0AF1\nt\x03u\x03u\x03u\x07u\u0AF6\nu\fu\x0Eu\u0AF9\vu\x03" +
		"v\x03v\x03v\x03v\x05v\u0AFF\nv\x03v\x05v\u0B02\nv\x03w\x03w\x03w\x07w" +
		"\u0B07\nw\fw\x0Ew\u0B0A\vw\x03x\x03x\x03x\x03x\x03x\x05x\u0B11\nx\x03" +
		"x\x05x\u0B14\nx\x03y\x03y\x03y\x03y\x03y\x03z\x03z\x03z\x03z\x07z\u0B1F" +
		"\nz\fz\x0Ez\u0B22\vz\x03{\x03{\x03{\x03{\x03|\x03|\x03|\x03|\x03|\x03" +
		"|\x03|\x03|\x03|\x03|\x03|\x07|\u0B33\n|\f|\x0E|\u0B36\v|\x03|\x03|\x03" +
		"|\x03|\x03|\x07|\u0B3D\n|\f|\x0E|\u0B40\v|\x05|\u0B42\n|\x03|\x03|\x03" +
		"|\x03|\x03|\x07|\u0B49\n|\f|\x0E|\u0B4C\v|\x05|\u0B4E\n|\x05|\u0B50\n" +
		"|\x03|\x05|\u0B53\n|\x03|\x05|\u0B56\n|\x03}\x03}\x03}\x03}\x03}\x03}" +
		"\x03}\x03}\x03}\x03}\x03}\x03}\x03}\x03}\x03}\x03}\x05}\u0B68\n}\x03~" +
		"\x03~\x03~\x03~\x03~\x03~\x03~\x05~\u0B71\n~\x03\x7F\x03\x7F\x03\x7F\x07" +
		"\x7F\u0B76\n\x7F\f\x7F\x0E\x7F\u0B79\v\x7F\x03\x80\x03\x80\x03\x80\x03" +
		"\x80\x05\x80\u0B7F\n\x80\x03\x81\x03\x81\x03\x81\x07\x81\u0B84\n\x81\f" +
		"\x81\x0E\x81\u0B87\v\x81\x03\x82\x03\x82\x03\x82\x03\x83\x03\x83\x06\x83" +
		"\u0B8E\n\x83\r\x83\x0E\x83\u0B8F\x03\x83\x05\x83\u0B93\n\x83\x03\x84\x03" +
		"\x84\x03\x84\x05\x84\u0B98\n\x84\x03\x85\x03\x85\x03\x85\x03\x85\x03\x85" +
		"\x03\x85\x05\x85\u0BA0\n\x85\x03\x86\x03\x86\x03\x87\x03\x87\x05\x87\u0BA6" +
		"\n\x87\x03\x87\x03\x87\x03\x87\x05\x87\u0BAB\n\x87\x03\x87\x03\x87\x03" +
		"\x87\x05\x87\u0BB0\n\x87\x03\x87\x03\x87\x05\x87\u0BB4\n\x87\x03\x87\x03" +
		"\x87\x05\x87\u0BB8\n\x87\x03\x87\x03\x87\x05\x87\u0BBC\n\x87\x03\x87\x03" +
		"\x87\x05\x87\u0BC0\n\x87\x03\x87\x03\x87\x05\x87\u0BC4\n\x87\x03\x87\x03" +
		"\x87\x05\x87\u0BC8\n\x87\x03\x87\x03\x87\x05\x87\u0BCC\n\x87\x03\x87\x05" +
		"\x87\u0BCF\n\x87\x03\x88\x03\x88\x03\x88\x03\x88\x03\x88\x03\x88\x03\x88" +
		"\x05\x88\u0BD8\n\x88\x03\x89\x03\x89\x03\x8A\x03\x8A\x03\x8B\x03\x8B\x03" +
		"\x8B\n\u039E\u03DC\u03E6\u03F7\u0400\u0409\u0412\u0419\x02\x06V\xC2\xC6" +
		"\xC8\x8C\x02\x02\x04\x02\x06\x02\b\x02\n\x02\f\x02\x0E\x02\x10\x02\x12" +
		"\x02\x14\x02\x16\x02\x18\x02\x1A\x02\x1C\x02\x1E\x02 \x02\"\x02$\x02&" +
		"\x02(\x02*\x02,\x02.\x020\x022\x024\x026\x028\x02:\x02<\x02>\x02@\x02" +
		"B\x02D\x02F\x02H\x02J\x02L\x02N\x02P\x02R\x02T\x02V\x02X\x02Z\x02\\\x02" +
		"^\x02`\x02b\x02d\x02f\x02h\x02j\x02l\x02n\x02p\x02r\x02t\x02v\x02x\x02" +
		"z\x02|\x02~\x02\x80\x02\x82\x02\x84\x02\x86\x02\x88\x02\x8A\x02\x8C\x02" +
		"\x8E\x02\x90\x02\x92\x02\x94\x02\x96\x02\x98\x02\x9A\x02\x9C\x02\x9E\x02" +
		"\xA0\x02\xA2\x02\xA4\x02\xA6\x02\xA8\x02\xAA\x02\xAC\x02\xAE\x02\xB0\x02" +
		"\xB2\x02\xB4\x02\xB6\x02\xB8\x02\xBA\x02\xBC\x02\xBE\x02\xC0\x02\xC2\x02" +
		"\xC4\x02\xC6\x02\xC8\x02\xCA\x02\xCC\x02\xCE\x02\xD0\x02\xD2\x02\xD4\x02" +
		"\xD6\x02\xD8\x02\xDA\x02\xDC\x02\xDE\x02\xE0\x02\xE2\x02\xE4\x02\xE6\x02" +
		"\xE8\x02\xEA\x02\xEC\x02\xEE\x02\xF0\x02\xF2\x02\xF4\x02\xF6\x02\xF8\x02" +
		"\xFA\x02\xFC\x02\xFE\x02\u0100\x02\u0102\x02\u0104\x02\u0106\x02\u0108" +
		"\x02\u010A\x02\u010C\x02\u010E\x02\u0110\x02\u0112\x02\u0114\x02\x02-" +
		"\x04\x02AA\xB2\xB2\x04\x02!!\xC0\xC0\x04\x02@@\x94\x94\x04\x02eeqq\x03" +
		"\x02,-\x04\x02\xE0\xE0\xFF\xFF\x04\x02\x10\x10$$\x07\x02))55WWdd\x8D\x8D" +
		"\x03\x02EF\x04\x02WWdd\x04\x02\x98\x98\u0119\u0119\x04\x02\r\r\x87\x87" +
		"\x04\x02\x89\x89\u0119\u0119\x05\x02??\x93\x93\xCA\xCA\x06\x02RRxx\xD2" +
		"\xD2\xF5\xF5\x05\x02RR\xD2\xD2\xF5\xF5\x04\x02\x18\x18EE\x04\x02__\x7F" +
		"\x7F\x04\x02\x0F\x0FJJ\x04\x02\u011D\u011D\u011F\u011F\x05\x02\x0F\x0F" +
		"\x14\x14\xD6\xD6\x05\x02ZZ\xEF\xEF\xF7\xF7\x04\x02\u010E\u010F\u0113\u0113" +
		"\x04\x02LL\u0110\u0112\x04\x02\u010E\u010F\u0116\u0116\x04\x02::<<\x03" +
		"\x02\xDE\xDF\x04\x02\x05\x05ee\x04\x02\x05\x05aa\x05\x02\x1C\x1C\x82\x82" +
		"\xEA\xEA\x03\x02\u0106\u010D\x04\x02LL\u010E\u0117\x06\x02\x12\x12qq\x97" +
		"\x97\x9F\x9F\x04\x02ZZ\xEF\xEF\x03\x02\u010E\u010F\x04\x02KK\xA8\xA8\x04" +
		"\x02\xA0\xA0\xD7\xD7\x04\x02``\xAF\xAF\x03\x02\u011E\u011F\x04\x02MM\xD1" +
		"\xD12\x02\r\x0E\x10\x11\x13\x13\x15\x16\x18\x19\x1B\x1B\x1D!$$&)++-35" +
		"589>IKMQQSY\\\\^`cdgillnprsuwyy||~\x81\x84\x94\x96\x96\x99\x9A\x9D\x9E" +
		"\xA1\xA1\xA3\xA4\xA6\xAF\xB1\xB9\xBB\xC1\xC3\xCA\xCC\xCF\xD1\xD5\xD7\xDF" +
		"\xE1\xE5\xE9\xE9\xEB\xF4\xF8\xFB\xFE\u0100\u0103\u0103\u0105\u0105\x11" +
		"\x02\x13\x1377RRffttxx}}\x83\x83\x95\x95\x9B\x9B\xC2\xC2\xCC\xCC\xD2\xD2" +
		"\xF5\xF5\xFD\xFD\x12\x02\r\x12\x1468QSegsuwy|~\x82\x84\x94\x96\x9A\x9C" +
		"\xC1\xC3\xCB\xCD\xD1\xD3\xF4\xF6\xFC\xFE\u0105\x02\u0DB9\x02\u0116\x03" +
		"\x02\x02\x02\x04\u0120\x03\x02\x02\x02\x06\u0123\x03\x02\x02\x02\b\u0125" +
		"\x03\x02\x02\x02\n\u0128\x03\x02\x02\x02\f\u012B\x03\x02\x02\x02\x0E\u012E" +
		"\x03\x02\x02\x02\x10\u0131\x03\x02\x02\x02\x12\u041C\x03\x02\x02\x02\x14" +
		"\u041E\x03\x02\x02\x02\x16\u04C8\x03\x02\x02\x02\x18\u04CA\x03\x02\x02" +
		"\x02\x1A\u04DB\x03\x02\x02\x02\x1C\u04E1\x03\x02\x02\x02\x1E\u04ED\x03" +
		"\x02\x02\x02 \u04FA\x03\x02\x02\x02\"\u04FD\x03\x02\x02\x02$\u0501\x03" +
		"\x02\x02\x02&\u053D\x03\x02\x02\x02(\u053F\x03\x02\x02\x02*\u0543\x03" +
		"\x02\x02\x02,\u054F\x03\x02\x02\x02.\u0554\x03\x02\x02\x020\u055B\x03" +
		"\x02\x02\x022\u055D\x03\x02\x02\x024\u0565\x03\x02\x02\x026\u056E\x03" +
		"\x02\x02\x028\u0579\x03\x02\x02\x02:\u0588\x03\x02\x02\x02<\u058B\x03" +
		"\x02\x02\x02>\u0596\x03\x02\x02\x02@\u05A6\x03\x02\x02\x02B\u05AC\x03" +
		"\x02\x02\x02D\u05AE\x03\x02\x02\x02F\u05B9\x03\x02\x02\x02H\u05CA\x03" +
		"\x02\x02\x02J\u05D1\x03\x02\x02\x02L\u05D3\x03\x02\x02\x02N\u05D9\x03" +
		"\x02\x02\x02P\u060F\x03\x02\x02\x02R\u061B\x03\x02\x02\x02T\u064B\x03" +
		"\x02\x02\x02V\u064E\x03\x02\x02\x02X\u0674\x03\x02\x02\x02Z\u0676\x03" +
		"\x02\x02\x02\\\u067E\x03\x02\x02\x02^\u069F\x03\x02\x02\x02`\u06BE\x03" +
		"\x02\x02\x02b\u06CA\x03\x02\x02\x02d\u06EA\x03\x02\x02\x02f\u06F6\x03" +
		"\x02\x02\x02h\u06F9\x03\x02\x02\x02j\u0702\x03\x02\x02\x02l\u0713\x03" +
		"\x02\x02\x02n\u0727\x03\x02\x02\x02p\u0729\x03\x02\x02\x02r\u0731\x03" +
		"\x02\x02\x02t\u0735\x03\x02\x02\x02v\u0738\x03\x02\x02\x02x\u073B\x03" +
		"\x02\x02\x02z\u0755\x03\x02\x02\x02|\u0757\x03\x02\x02\x02~\u076C\x03" +
		"\x02\x02\x02\x80\u079A\x03\x02\x02\x02\x82\u07A9\x03\x02\x02\x02\x84\u07AB" +
		"\x03\x02\x02\x02\x86\u07C9\x03\x02\x02\x02\x88\u07CB\x03\x02\x02\x02\x8A" +
		"\u07D2\x03\x02\x02\x02\x8C\u07F2\x03\x02\x02\x02\x8E\u07F4\x03\x02\x02" +
		"\x02\x90\u0806\x03\x02\x02\x02\x92\u0820\x03\x02\x02\x02\x94\u0826\x03" +
		"\x02\x02\x02\x96\u0828\x03\x02\x02\x02\x98\u0847\x03\x02\x02\x02\x9A\u0849" +
		"\x03\x02\x02\x02\x9C\u084D\x03\x02\x02\x02\x9E\u0855\x03\x02\x02\x02\xA0" +
		"\u0860\x03\x02\x02\x02\xA2\u0864\x03\x02\x02\x02\xA4\u086F\x03\x02\x02" +
		"\x02\xA6\u088B\x03\x02\x02\x02\xA8\u088D\x03\x02\x02\x02\xAA\u0898\x03" +
		"\x02\x02\x02\xAC\u08AE\x03\x02\x02\x02\xAE\u08E1\x03\x02\x02\x02\xB0\u08E3" +
		"\x03\x02\x02\x02\xB2\u08EB\x03\x02\x02\x02\xB4\u08F6\x03\x02\x02\x02\xB6" +
		"\u08FA\x03\x02\x02\x02\xB8\u0904\x03\x02\x02\x02\xBA\u090C\x03\x02\x02" +
		"\x02\xBC\u0924\x03\x02\x02\x02\xBE\u0928\x03\x02\x02\x02\xC0\u092A\x03" +
		"\x02\x02\x02\xC2\u0938\x03\x02\x02\x02\xC4\u0997\x03\x02\x02\x02\xC6\u099D" +
		"\x03\x02\x02\x02\xC8\u0A6F\x03\x02\x02\x02\xCA\u0A8A\x03\x02\x02\x02\xCC" +
		"\u0A8C\x03\x02\x02\x02\xCE\u0A8E\x03\x02\x02\x02\xD0\u0A90\x03\x02\x02" +
		"\x02\xD2\u0A92\x03\x02\x02\x02\xD4\u0A94\x03\x02\x02\x02\xD6\u0A99\x03" +
		"\x02\x02\x02\xD8\u0AA0\x03\x02\x02\x02\xDA\u0AA4\x03\x02\x02\x02\xDC\u0AA9" +
		"\x03\x02\x02\x02\xDE\u0AB3\x03\x02\x02\x02\xE0\u0AB8\x03\x02\x02\x02\xE2" +
		"\u0ADC\x03\x02\x02\x02\xE4\u0ADE\x03\x02\x02\x02\xE6\u0AE6\x03\x02\x02" +
		"\x02\xE8\u0AF2\x03\x02\x02\x02\xEA\u0AFA\x03\x02\x02\x02\xEC\u0B03\x03" +
		"\x02\x02\x02\xEE\u0B0B\x03\x02\x02\x02\xF0\u0B15\x03\x02\x02\x02\xF2\u0B1A" +
		"\x03\x02\x02\x02\xF4\u0B23\x03\x02\x02\x02\xF6\u0B55\x03\x02\x02\x02\xF8" +
		"\u0B67\x03\x02\x02\x02\xFA\u0B70\x03\x02\x02\x02\xFC\u0B72\x03\x02\x02" +
		"\x02\xFE\u0B7E\x03\x02\x02\x02\u0100\u0B80\x03\x02\x02\x02\u0102\u0B88" +
		"\x03\x02\x02\x02\u0104\u0B92\x03\x02\x02\x02\u0106\u0B97\x03\x02\x02\x02" +
		"\u0108\u0B9F\x03\x02\x02\x02\u010A\u0BA1\x03\x02\x02\x02\u010C\u0BCE\x03" +
		"\x02\x02\x02\u010E\u0BD7\x03\x02\x02\x02\u0110\u0BD9\x03\x02\x02\x02\u0112" +
		"\u0BDB\x03\x02\x02\x02\u0114\u0BDD\x03\x02\x02\x02\u0116\u0117\x05\x04" +
		"\x03\x02\u0117\u0118\x07\x02\x02\x03\u0118\x03\x03\x02\x02\x02\u0119\u011B" +
		"\x05\x12\n\x02\u011A\u011C\x07\u0118\x02\x02\u011B\u011A\x03\x02\x02\x02" +
		"\u011B\u011C\x03\x02\x02\x02\u011C\u011F\x03\x02\x02\x02\u011D\u011F\x05" +
		"\x06\x04\x02\u011E\u0119\x03\x02\x02\x02\u011E\u011D\x03\x02\x02\x02\u011F" +
		"\u0122\x03\x02\x02\x02\u0120\u011E\x03\x02\x02\x02\u0120\u0121\x03\x02" +
		"\x02\x02\u0121\x05\x03\x02\x02\x02\u0122\u0120\x03\x02\x02\x02\u0123\u0124" +
		"\x07\u0118\x02\x02\u0124\x07\x03\x02\x02\x02\u0125\u0126\x05\xB6\\\x02" +
		"\u0126\u0127\x07\x02\x02\x03\u0127\t\x03\x02\x02\x02\u0128\u0129\x05\xB4" +
		"[\x02\u0129\u012A\x07\x02\x02\x03\u012A\v\x03\x02\x02\x02\u012B\u012C" +
		"\x05\xB2Z\x02\u012C\u012D\x07\x02\x02\x03\u012D\r\x03\x02\x02\x02\u012E" +
		"\u012F\x05\xE2r\x02\u012F\u0130\x07\x02\x02\x03\u0130\x0F\x03\x02\x02" +
		"\x02\u0131\u0132\x05\xE8u\x02\u0132\u0133\x07\x02\x02\x03\u0133\x11\x03" +
		"\x02\x02\x02\u0134\u041D\x05$\x13\x02\u0135\u0137\x054\x1B\x02\u0136\u0135" +
		"\x03\x02\x02\x02\u0136\u0137\x03\x02\x02\x02\u0137\u0138\x03\x02\x02\x02" +
		"\u0138\u041D\x05P)\x02\u0139\u013B\x07\xFB\x02\x02\u013A\u013C\x07\x93" +
		"\x02\x02\u013B\u013A\x03\x02\x02\x02\u013B\u013C\x03\x02\x02\x02\u013C" +
		"\u013D\x03\x02\x02\x02\u013D\u041D\x05\xB2Z\x02\u013E\u013F\x076\x02\x02" +
		"\u013F\u0143\x05.\x18\x02\u0140\u0141\x07n\x02\x02\u0141\u0142\x07\x97" +
		"\x02\x02\u0142\u0144\x07T\x02\x02\u0143\u0140\x03\x02\x02\x02\u0143\u0144" +
		"\x03\x02\x02\x02\u0144\u0145\x03\x02\x02\x02\u0145\u014D\x05\xB2Z\x02" +
		"\u0146\u014C\x05\"\x12\x02\u0147\u014C\x05 \x11\x02\u0148\u0149\x07\u0104" +
		"\x02\x02\u0149\u014A\t\x02\x02\x02\u014A\u014C\x05<\x1F\x02\u014B\u0146" +
		"\x03\x02\x02\x02\u014B\u0147\x03\x02\x02\x02\u014B\u0148\x03\x02\x02\x02" +
		"\u014C\u014F\x03\x02\x02\x02\u014D\u014B\x03\x02\x02\x02\u014D\u014E\x03" +
		"\x02\x02\x02\u014E\u041D\x03\x02\x02\x02\u014F\u014D\x03\x02\x02\x02\u0150" +
		"\u0151\x07\x10\x02\x02\u0151\u0152\x05.\x18\x02\u0152\u0153\x05\xB2Z\x02" +
		"\u0153\u0154\x07\xD1\x02\x02\u0154\u0155\t\x02\x02\x02\u0155\u0156\x05" +
		"<\x1F\x02\u0156\u041D\x03\x02\x02\x02\u0157\u0158\x07\x10\x02\x02\u0158" +
		"\u0159\x05.\x18\x02\u0159\u015A\x05\xB2Z\x02\u015A\u015B\x07\xD1\x02\x02" +
		"\u015B\u015C\x05 \x11\x02\u015C\u041D\x03\x02\x02\x02\u015D\u015E\x07" +
		"M\x02\x02\u015E\u0161\x05.\x18\x02\u015F\u0160\x07n\x02\x02\u0160\u0162" +
		"\x07T\x02\x02\u0161\u015F\x03\x02\x02\x02\u0161\u0162\x03\x02\x02\x02" +
		"\u0162\u0163\x03\x02\x02\x02\u0163\u0165\x05\xB2Z\x02\u0164\u0166\t\x03" +
		"\x02\x02\u0165\u0164\x03\x02\x02\x02\u0165\u0166\x03\x02\x02\x02\u0166" +
		"\u041D\x03\x02\x02\x02\u0167\u0168\x07\xD4\x02\x02\u0168\u016B\t\x04\x02" +
		"\x02\u0169\u016A\t\x05\x02\x02\u016A\u016C\x05\xB2Z\x02\u016B\u0169\x03" +
		"\x02\x02\x02\u016B\u016C\x03\x02\x02\x02\u016C\u0171\x03\x02\x02\x02\u016D" +
		"\u016F\x07\x84\x02\x02\u016E\u016D\x03\x02\x02\x02\u016E\u016F\x03\x02" +
		"\x02\x02\u016F\u0170\x03\x02\x02\x02\u0170\u0172\x07\u0119\x02\x02\u0171" +
		"\u016E\x03\x02\x02\x02\u0171\u0172\x03\x02\x02\x02\u0172\u041D\x03\x02" +
		"\x02\x02\u0173\u0178\x05\x18\r\x02\u0174\u0175\x07\x03\x02\x02\u0175\u0176" +
		"\x05\xE8u\x02\u0176\u0177\x07\x04\x02\x02\u0177\u0179\x03\x02\x02\x02" +
		"\u0178\u0174\x03\x02\x02\x02\u0178\u0179\x03\x02\x02\x02\u0179\u017A\x03" +
		"\x02\x02\x02\u017A\u017B\x058\x1D\x02\u017B\u0180\x05:\x1E\x02\u017C\u017E" +
		"\x07\x17\x02\x02\u017D\u017C\x03\x02\x02\x02\u017D\u017E\x03\x02\x02\x02" +
		"\u017E\u017F\x03\x02\x02\x02\u017F\u0181\x05$\x13\x02\u0180\u017D\x03" +
		"\x02\x02\x02\u0180\u0181\x03\x02\x02\x02\u0181\u041D\x03\x02\x02\x02\u0182" +
		"\u0187\x05\x18\r\x02\u0183\u0184\x07\x03\x02\x02\u0184\u0185\x05\xE8u" +
		"\x02\u0185\u0186\x07\x04\x02\x02\u0186\u0188\x03\x02\x02\x02\u0187\u0183" +
		"\x03\x02\x02\x02\u0187\u0188\x03\x02\x02\x02\u0188\u019E\x03\x02\x02\x02" +
		"\u0189\u019D\x05\"\x12\x02\u018A\u018B\x07\xA9\x02\x02\u018B\u018C\x07" +
		"\x1F\x02\x02\u018C\u018D\x07\x03\x02\x02\u018D\u018E\x05\xE8u\x02\u018E" +
		"\u018F\x07\x04\x02\x02\u018F\u0194\x03\x02\x02\x02\u0190\u0191\x07\xA9" +
		"\x02\x02\u0191\u0192\x07\x1F\x02\x02\u0192\u0194\x05\x9AN\x02\u0193\u018A" +
		"\x03\x02\x02\x02\u0193\u0190\x03\x02\x02\x02\u0194\u019D\x03\x02\x02\x02" +
		"\u0195\u019D\x05\x1C\x0F\x02\u0196\u019D\x05\x1E\x10\x02\u0197\u019D\x05" +
		"\xAEX\x02\u0198\u019D\x05H%\x02\u0199\u019D\x05 \x11\x02\u019A\u019B\x07" +
		"\xE3\x02\x02\u019B\u019D\x05<\x1F\x02\u019C\u0189\x03\x02\x02\x02\u019C" +
		"\u0193\x03\x02\x02\x02\u019C\u0195\x03\x02\x02\x02\u019C\u0196\x03\x02" +
		"\x02\x02\u019C\u0197\x03\x02\x02\x02\u019C\u0198\x03\x02\x02\x02\u019C" +
		"\u0199\x03\x02\x02\x02\u019C\u019A\x03\x02\x02\x02\u019D\u01A0\x03\x02" +
		"\x02\x02\u019E\u019C\x03\x02\x02\x02\u019E\u019F\x03\x02\x02\x02\u019F" +
		"\u01A5\x03\x02\x02\x02\u01A0\u019E\x03\x02\x02\x02\u01A1\u01A3\x07\x17" +
		"\x02\x02\u01A2\u01A1\x03\x02\x02\x02\u01A2\u01A3\x03\x02\x02\x02\u01A3" +
		"\u01A4\x03\x02\x02\x02\u01A4\u01A6\x05$\x13\x02\u01A5\u01A2\x03\x02\x02" +
		"\x02\u01A5\u01A6\x03\x02\x02\x02\u01A6\u041D\x03\x02\x02\x02\u01A7\u01A8" +
		"\x076\x02\x02\u01A8\u01AC\x07\xE0\x02\x02\u01A9\u01AA\x07n\x02\x02\u01AA" +
		"\u01AB\x07\x97\x02\x02\u01AB\u01AD\x07T\x02\x02\u01AC\u01A9\x03\x02\x02" +
		"\x02\u01AC\u01AD\x03\x02\x02\x02\u01AD\u01AE\x03\x02\x02\x02\u01AE\u01AF" +
		"\x05\xB4[\x02\u01AF\u01B0\x07\x84\x02\x02\u01B0\u01B9\x05\xB4[\x02\u01B1" +
		"\u01B8\x058\x1D\x02\u01B2\u01B8\x05\xAEX\x02\u01B3\u01B8\x05H%\x02\u01B4" +
		"\u01B8\x05 \x11\x02\u01B5\u01B6\x07\xE3\x02\x02\u01B6\u01B8\x05<\x1F\x02" +
		"\u01B7\u01B1\x03\x02\x02\x02\u01B7\u01B2\x03\x02\x02\x02\u01B7\u01B3\x03" +
		"\x02\x02\x02\u01B7\u01B4\x03\x02\x02\x02\u01B7\u01B5\x03\x02\x02\x02\u01B8" +
		"\u01BB\x03\x02\x02\x02\u01B9\u01B7\x03\x02\x02\x02\u01B9\u01BA\x03\x02" +
		"\x02\x02\u01BA\u041D\x03\x02\x02\x02\u01BB\u01B9\x03\x02\x02\x02\u01BC" +
		"\u01C1\x05\x1A\x0E\x02\u01BD\u01BE\x07\x03\x02\x02\u01BE\u01BF\x05\xE8" +
		"u\x02\u01BF\u01C0\x07\x04\x02\x02\u01C0\u01C2\x03\x02\x02\x02\u01C1\u01BD" +
		"\x03\x02\x02\x02\u01C1\u01C2\x03\x02\x02\x02\u01C2\u01C3\x03\x02\x02\x02" +
		"\u01C3\u01C4\x058\x1D\x02\u01C4\u01C9\x05:\x1E\x02\u01C5\u01C7\x07\x17" +
		"\x02\x02\u01C6\u01C5\x03\x02\x02\x02\u01C6\u01C7\x03\x02\x02\x02\u01C7" +
		"\u01C8\x03\x02\x02\x02\u01C8\u01CA\x05$\x13\x02\u01C9\u01C6\x03\x02\x02" +
		"\x02\u01C9\u01CA\x03\x02\x02\x02\u01CA\u041D\x03\x02\x02\x02\u01CB\u01CC" +
		"\x07\x11\x02\x02\u01CC\u01CD\x07\xE0\x02\x02\u01CD\u01CF\x05\xB2Z\x02" +
		"\u01CE\u01D0\x05*\x16\x02\u01CF\u01CE\x03\x02\x02\x02\u01CF\u01D0\x03" +
		"\x02\x02\x02\u01D0\u01D1\x03\x02\x02\x02\u01D1\u01D2\x072\x02\x02\u01D2" +
		"\u01DA\x07\xDA\x02\x02\u01D3\u01DB\x05\u0106\x84\x02\u01D4\u01D5\x07a" +
		"\x02\x02\u01D5\u01D6\x07-\x02\x02\u01D6\u01DB\x05\x9CO\x02\u01D7\u01D8" +
		"\x07a\x02\x02\u01D8\u01D9\x07\x0F\x02\x02\u01D9\u01DB\x07-\x02\x02\u01DA" +
		"\u01D3\x03\x02\x02\x02\u01DA\u01D4\x03\x02\x02\x02\u01DA\u01D7\x03\x02" +
		"\x02\x02\u01DA\u01DB\x03\x02\x02\x02\u01DB\u041D\x03\x02\x02\x02\u01DC" +
		"\u01DD\x07\x10\x02\x02\u01DD\u01DE\x07\xE0\x02\x02\u01DE\u01DF\x05\xB2" +
		"Z\x02\u01DF\u01E0\x07\r\x02\x02\u01E0\u01E1\t\x06\x02\x02\u01E1\u01E2" +
		"\x05\xE4s\x02\u01E2\u041D\x03\x02\x02\x02\u01E3\u01E4\x07\x10\x02\x02" +
		"\u01E4\u01E5\x07\xE0\x02\x02\u01E5\u01E6\x05\xB2Z\x02\u01E6\u01E7\x07" +
		"\r\x02\x02\u01E7\u01E8\t\x06\x02\x02\u01E8\u01E9\x07\x03\x02\x02\u01E9" +
		"\u01EA\x05\xE4s\x02\u01EA\u01EB\x07\x04\x02\x02\u01EB\u041D\x03\x02\x02" +
		"\x02\u01EC\u01ED\x07\x10\x02\x02\u01ED\u01EE\x07\xE0\x02\x02\u01EE\u01EF" +
		"\x05\xB2Z\x02\u01EF\u01F0\x07\xBC\x02\x02\u01F0\u01F1\x07,\x02\x02\u01F1" +
		"\u01F2\x05\xB2Z\x02\u01F2\u01F3\x07\xE8\x02\x02\u01F3\u01F4\x05\u0102" +
		"\x82\x02\u01F4\u041D\x03\x02\x02\x02\u01F5\u01F6\x07\x10\x02\x02\u01F6" +
		"\u01F7\x07\xE0\x02\x02\u01F7\u01F8\x05\xB2Z\x02\u01F8\u01F9\x07M\x02\x02" +
		"\u01F9\u01FA\t\x06\x02\x02\u01FA\u01FB\x07\x03\x02\x02\u01FB\u01FC\x05" +
		"\xB0Y\x02\u01FC\u01FD\x07\x04\x02\x02\u01FD\u041D\x03\x02\x02\x02\u01FE" +
		"\u01FF\x07\x10\x02\x02\u01FF\u0200\x07\xE0\x02\x02\u0200\u0201\x05\xB2" +
		"Z\x02\u0201\u0202\x07M\x02\x02\u0202\u0203\t\x06\x02\x02\u0203\u0204\x05" +
		"\xB0Y\x02\u0204\u041D\x03\x02\x02\x02\u0205\u0206\x07\x10\x02\x02\u0206" +
		"\u0207\t\x07\x02\x02\u0207\u0208\x05\xB2Z\x02\u0208\u0209\x07\xBC\x02" +
		"\x02\u0209\u020A\x07\xE8\x02\x02\u020A\u020B\x05\xB2Z\x02\u020B\u041D" +
		"\x03\x02\x02\x02\u020C\u020D\x07\x10\x02\x02\u020D\u020E\t\x07\x02\x02" +
		"\u020E\u020F\x05\xB2Z\x02\u020F\u0210\x07\xD1\x02\x02\u0210\u0211\x07" +
		"\xE3\x02\x02\u0211\u0212\x05<\x1F\x02\u0212\u041D\x03\x02\x02\x02\u0213" +
		"\u0214\x07\x10\x02\x02\u0214\u0215\t\x07\x02\x02\u0215\u0216\x05\xB2Z" +
		"\x02\u0216\u0217\x07\xF9\x02\x02\u0217\u021A\x07\xE3\x02\x02\u0218\u0219" +
		"\x07n\x02\x02\u0219\u021B\x07T\x02\x02\u021A\u0218\x03\x02\x02\x02\u021A" +
		"\u021B\x03\x02\x02\x02\u021B\u021C\x03\x02\x02\x02\u021C\u021D\x05<\x1F" +
		"\x02\u021D\u041D\x03\x02\x02\x02\u021E\u021F\x07\x10\x02\x02\u021F\u0220" +
		"\x07\xE0\x02\x02\u0220\u0221\x05\xB2Z\x02\u0221\u0223\t\b\x02\x02\u0222" +
		"\u0224\x07,\x02\x02\u0223\u0222\x03\x02\x02\x02\u0223\u0224\x03\x02\x02" +
		"\x02\u0224\u0225\x03\x02\x02\x02\u0225\u0227\x05\xB2Z\x02\u0226\u0228" +
		"\x05\u010E\x88\x02";
	private static readonly _serializedATNSegment2: string =
		"\u0227\u0226\x03\x02\x02\x02\u0227\u0228\x03\x02\x02\x02\u0228\u041D\x03" +
		"\x02\x02\x02\u0229\u022A\x07\x10\x02\x02\u022A\u022B\x07\xE0\x02\x02\u022B" +
		"\u022D\x05\xB2Z\x02\u022C\u022E\x05*\x16\x02\u022D\u022C\x03\x02\x02\x02" +
		"\u022D\u022E\x03\x02\x02\x02\u022E\u022F\x03\x02\x02\x02\u022F\u0231\x07" +
		"$\x02\x02\u0230\u0232\x07,\x02\x02\u0231\u0230\x03\x02\x02\x02\u0231\u0232" +
		"\x03\x02\x02\x02\u0232\u0233\x03\x02\x02\x02\u0233\u0234\x05\xB2Z\x02" +
		"\u0234\u0236\x05\xEAv\x02\u0235\u0237\x05\xE0q\x02\u0236\u0235\x03\x02" +
		"\x02\x02\u0236\u0237\x03\x02\x02\x02\u0237\u041D\x03\x02\x02\x02\u0238" +
		"\u0239\x07\x10\x02\x02\u0239\u023A\x07\xE0\x02\x02\u023A\u023C\x05\xB2" +
		"Z\x02\u023B\u023D\x05*\x16\x02\u023C\u023B\x03\x02\x02\x02\u023C\u023D" +
		"\x03\x02\x02\x02\u023D\u023E\x03\x02\x02\x02\u023E\u023F\x07\xBE\x02\x02" +
		"\u023F\u0240\x07-\x02\x02\u0240\u0241\x07\x03\x02\x02\u0241\u0242\x05" +
		"\xE4s\x02\u0242\u0243\x07\x04\x02\x02\u0243\u041D\x03\x02\x02\x02\u0244" +
		"\u0245\x07\x10\x02\x02\u0245\u0246\x07\xE0\x02\x02\u0246\u0248\x05\xB2" +
		"Z\x02\u0247\u0249\x05*\x16\x02\u0248\u0247\x03\x02\x02\x02\u0248\u0249" +
		"\x03\x02\x02\x02\u0249\u024A\x03\x02\x02\x02\u024A\u024B\x07\xD1\x02\x02" +
		"\u024B\u024C\x07\xCE\x02\x02\u024C\u0250\x07\u0119\x02\x02\u024D\u024E" +
		"\x07\u0104\x02\x02\u024E\u024F\x07\xCF\x02\x02\u024F\u0251\x05<\x1F\x02" +
		"\u0250\u024D\x03\x02\x02\x02\u0250\u0251\x03\x02\x02\x02\u0251\u041D\x03" +
		"\x02\x02\x02\u0252\u0253\x07\x10\x02\x02\u0253\u0254\x07\xE0\x02\x02\u0254" +
		"\u0256\x05\xB2Z\x02\u0255\u0257\x05*\x16\x02\u0256\u0255\x03\x02\x02\x02" +
		"\u0256\u0257\x03\x02\x02\x02\u0257\u0258\x03\x02\x02\x02\u0258\u0259\x07" +
		"\xD1\x02\x02\u0259\u025A\x07\xCF\x02\x02\u025A\u025B\x05<\x1F\x02\u025B" +
		"\u041D\x03\x02\x02\x02\u025C\u025D\x07\x10\x02\x02\u025D\u025E\t\x07\x02" +
		"\x02\u025E\u025F\x05\xB2Z\x02\u025F\u0263\x07\r\x02\x02\u0260\u0261\x07" +
		"n\x02\x02\u0261\u0262\x07\x97\x02\x02\u0262\u0264\x07T\x02\x02\u0263\u0260" +
		"\x03\x02\x02\x02\u0263\u0264\x03\x02\x02\x02\u0264\u0266\x03\x02\x02\x02" +
		"\u0265\u0267\x05(\x15\x02\u0266\u0265\x03\x02\x02\x02\u0267\u0268\x03" +
		"\x02\x02\x02\u0268\u0266\x03\x02\x02\x02\u0268\u0269\x03\x02\x02\x02\u0269" +
		"\u041D\x03\x02\x02\x02\u026A\u026B\x07\x10\x02\x02\u026B\u026C\x07\xE0" +
		"\x02\x02\u026C\u026D\x05\xB2Z\x02\u026D\u026E\x05*\x16\x02\u026E\u026F" +
		"\x07\xBC\x02\x02\u026F\u0270\x07\xE8\x02\x02\u0270\u0271\x05*\x16\x02" +
		"\u0271\u041D\x03\x02\x02\x02\u0272\u0273\x07\x10\x02\x02\u0273\u0274\t" +
		"\x07\x02\x02\u0274\u0275\x05\xB2Z\x02\u0275\u0278\x07M\x02\x02\u0276\u0277" +
		"\x07n\x02\x02\u0277\u0279\x07T\x02\x02\u0278\u0276\x03\x02\x02\x02\u0278" +
		"\u0279\x03\x02\x02\x02\u0279\u027A\x03\x02\x02\x02\u027A\u027F\x05*\x16" +
		"\x02\u027B\u027C\x07\x05\x02\x02\u027C\u027E\x05*\x16\x02\u027D\u027B" +
		"\x03\x02\x02\x02\u027E\u0281\x03\x02\x02\x02\u027F\u027D\x03\x02\x02\x02" +
		"\u027F\u0280\x03\x02\x02\x02\u0280\u0283\x03\x02\x02\x02\u0281\u027F\x03" +
		"\x02\x02\x02\u0282\u0284\x07\xB3\x02\x02\u0283\u0282\x03\x02\x02\x02\u0283" +
		"\u0284\x03\x02\x02\x02\u0284\u041D\x03\x02\x02\x02\u0285\u0286\x07\x10" +
		"\x02\x02\u0286\u0287\x07\xE0\x02\x02\u0287\u0289\x05\xB2Z\x02\u0288\u028A" +
		"\x05*\x16\x02\u0289\u0288\x03\x02\x02\x02\u0289\u028A\x03\x02\x02\x02" +
		"\u028A\u028B\x03\x02\x02\x02\u028B\u028C\x07\xD1\x02\x02\u028C\u028D\x05" +
		" \x11\x02\u028D\u041D\x03\x02\x02\x02\u028E\u028F\x07\x10\x02\x02\u028F" +
		"\u0290\x07\xE0\x02\x02\u0290\u0291\x05\xB2Z\x02\u0291\u0292\x07\xB8\x02" +
		"\x02\u0292\u0293\x07\xAA\x02\x02\u0293\u041D\x03\x02\x02\x02\u0294\u0295" +
		"\x07M\x02\x02\u0295\u0298\x07\xE0\x02\x02\u0296\u0297\x07n\x02\x02\u0297" +
		"\u0299\x07T\x02\x02\u0298\u0296\x03\x02\x02\x02\u0298\u0299\x03\x02\x02" +
		"\x02\u0299\u029A\x03\x02\x02\x02\u029A\u029C\x05\xB2Z\x02\u029B\u029D" +
		"\x07\xB3\x02\x02\u029C\u029B\x03\x02\x02\x02\u029C\u029D\x03\x02\x02\x02" +
		"\u029D\u041D\x03\x02\x02\x02\u029E\u029F\x07M\x02\x02\u029F\u02A2\x07" +
		"\xFF\x02\x02\u02A0\u02A1\x07n\x02\x02\u02A1\u02A3\x07T\x02\x02\u02A2\u02A0" +
		"\x03\x02\x02\x02\u02A2\u02A3\x03\x02\x02\x02\u02A3\u02A4\x03\x02\x02\x02" +
		"\u02A4\u041D\x05\xB2Z\x02\u02A5\u02A8\x076\x02\x02\u02A6\u02A7\x07\x9F" +
		"\x02\x02\u02A7\u02A9\x07\xBE\x02\x02\u02A8\u02A6\x03\x02\x02\x02\u02A8" +
		"\u02A9\x03\x02\x02\x02\u02A9\u02AE\x03\x02\x02\x02\u02AA\u02AC\x07i\x02" +
		"\x02\u02AB\u02AA\x03\x02\x02\x02\u02AB\u02AC\x03\x02\x02\x02\u02AC\u02AD" +
		"\x03\x02\x02\x02\u02AD\u02AF\x07\xE4\x02\x02\u02AE\u02AB\x03\x02\x02\x02" +
		"\u02AE\u02AF\x03\x02\x02\x02\u02AF\u02B0\x03\x02\x02\x02\u02B0\u02B4\x07" +
		"\xFF\x02\x02\u02B1\u02B2\x07n\x02\x02\u02B2\u02B3\x07\x97\x02\x02\u02B3" +
		"\u02B5\x07T\x02\x02\u02B4\u02B1\x03\x02\x02\x02\u02B4\u02B5\x03\x02\x02" +
		"\x02\u02B5\u02B6\x03\x02\x02\x02\u02B6\u02B8\x05\xB2Z\x02\u02B7\u02B9" +
		"\x05\xA2R\x02\u02B8\u02B7\x03\x02\x02\x02\u02B8\u02B9\x03\x02\x02\x02" +
		"\u02B9\u02C2\x03\x02\x02\x02\u02BA\u02C1\x05\"\x12\x02\u02BB\u02BC\x07" +
		"\xA9\x02\x02\u02BC\u02BD\x07\x9B\x02\x02\u02BD\u02C1\x05\x9AN\x02\u02BE" +
		"\u02BF\x07\xE3\x02\x02\u02BF\u02C1\x05<\x1F\x02\u02C0\u02BA\x03\x02\x02" +
		"\x02\u02C0\u02BB\x03\x02\x02\x02\u02C0\u02BE\x03\x02\x02\x02\u02C1\u02C4" +
		"\x03\x02\x02\x02\u02C2\u02C0\x03\x02\x02\x02\u02C2\u02C3\x03\x02\x02\x02" +
		"\u02C3\u02C5\x03\x02\x02\x02\u02C4\u02C2\x03\x02\x02\x02\u02C5\u02C6\x07" +
		"\x17\x02\x02\u02C6\u02C7\x05$\x13\x02\u02C7\u041D\x03\x02\x02\x02\u02C8" +
		"\u02CB\x076\x02\x02\u02C9\u02CA\x07\x9F\x02\x02\u02CA\u02CC\x07\xBE\x02" +
		"\x02\u02CB\u02C9\x03\x02\x02\x02\u02CB\u02CC\x03\x02\x02\x02\u02CC\u02CE" +
		"\x03\x02\x02\x02\u02CD\u02CF\x07i\x02\x02\u02CE\u02CD\x03\x02\x02\x02" +
		"\u02CE\u02CF\x03\x02\x02\x02\u02CF\u02D0\x03\x02\x02\x02\u02D0\u02D1\x07" +
		"\xE4\x02\x02\u02D1\u02D2\x07\xFF\x02\x02\u02D2\u02D7\x05\xB4[\x02\u02D3" +
		"\u02D4\x07\x03\x02\x02\u02D4\u02D5\x05\xE8u\x02\u02D5\u02D6\x07\x04\x02" +
		"\x02\u02D6\u02D8\x03\x02\x02\x02\u02D7\u02D3\x03\x02\x02\x02\u02D7\u02D8" +
		"\x03\x02\x02\x02\u02D8\u02D9\x03\x02\x02\x02\u02D9\u02DC\x058\x1D\x02" +
		"\u02DA\u02DB\x07\x9E\x02\x02\u02DB\u02DD\x05<\x1F\x02\u02DC\u02DA\x03" +
		"\x02\x02\x02\u02DC\u02DD\x03\x02\x02\x02\u02DD\u041D\x03\x02\x02\x02\u02DE" +
		"\u02DF\x07\x10\x02\x02\u02DF\u02E0\x07\xFF\x02\x02\u02E0\u02E2\x05\xB2" +
		"Z\x02\u02E1\u02E3\x07\x17\x02\x02\u02E2\u02E1\x03\x02\x02\x02\u02E2\u02E3" +
		"\x03\x02\x02\x02\u02E3\u02E4\x03\x02\x02\x02\u02E4\u02E5\x05$\x13\x02" +
		"\u02E5\u041D\x03\x02\x02\x02\u02E6\u02E9\x076\x02\x02\u02E7\u02E8\x07" +
		"\x9F\x02\x02\u02E8\u02EA\x07\xBE\x02\x02\u02E9\u02E7\x03\x02\x02\x02\u02E9" +
		"\u02EA\x03\x02\x02\x02\u02EA\u02EC\x03\x02\x02\x02\u02EB\u02ED\x07\xE4" +
		"\x02\x02\u02EC\u02EB\x03\x02\x02\x02\u02EC\u02ED\x03\x02\x02\x02\u02ED" +
		"\u02EE\x03\x02\x02\x02\u02EE\u02F2\x07g\x02\x02\u02EF\u02F0\x07n\x02\x02" +
		"\u02F0\u02F1\x07\x97\x02\x02\u02F1\u02F3\x07T\x02\x02\u02F2\u02EF\x03" +
		"\x02\x02\x02\u02F2\u02F3\x03\x02\x02\x02\u02F3\u02F4\x03\x02\x02\x02\u02F4" +
		"\u02F5\x05\xB2Z\x02\u02F5\u02F6\x07\x17\x02\x02\u02F6\u0300\x07\u0119" +
		"\x02\x02\u02F7\u02F8\x07\xFD\x02\x02\u02F8\u02FD\x05N(\x02\u02F9\u02FA" +
		"\x07\x05\x02\x02\u02FA\u02FC\x05N(\x02\u02FB\u02F9\x03\x02\x02\x02\u02FC" +
		"\u02FF\x03\x02\x02\x02\u02FD\u02FB\x03\x02\x02\x02\u02FD\u02FE\x03\x02" +
		"\x02\x02\u02FE\u0301\x03\x02\x02\x02\u02FF\u02FD\x03\x02\x02\x02\u0300" +
		"\u02F7\x03\x02\x02\x02\u0300\u0301\x03\x02\x02\x02\u0301\u041D\x03\x02" +
		"\x02\x02\u0302\u0304\x07M\x02\x02\u0303\u0305\x07\xE4\x02\x02\u0304\u0303" +
		"\x03\x02\x02\x02\u0304\u0305\x03\x02\x02\x02\u0305\u0306\x03\x02\x02\x02" +
		"\u0306\u0309\x07g\x02\x02\u0307\u0308\x07n\x02\x02\u0308\u030A\x07T\x02" +
		"\x02\u0309\u0307\x03\x02\x02\x02\u0309\u030A\x03\x02\x02\x02\u030A\u030B" +
		"\x03\x02\x02\x02\u030B\u041D\x05\xB2Z\x02\u030C\u030E\x07U\x02\x02\u030D" +
		"\u030F\t\t\x02\x02\u030E\u030D\x03\x02\x02\x02\u030E\u030F\x03\x02\x02" +
		"\x02\u030F\u0310\x03\x02\x02\x02\u0310\u041D\x05\x12\n\x02\u0311\u0312" +
		"\x07\xD4\x02\x02\u0312\u0315\x07\xE1\x02\x02\u0313\u0314\t\x05\x02\x02" +
		"\u0314\u0316\x05\xB2Z\x02\u0315\u0313\x03\x02\x02\x02\u0315\u0316\x03" +
		"\x02\x02\x02\u0316\u031B\x03\x02\x02\x02\u0317\u0319\x07\x84\x02\x02\u0318" +
		"\u0317\x03\x02\x02\x02\u0318\u0319\x03\x02\x02\x02\u0319\u031A\x03\x02" +
		"\x02\x02\u031A\u031C\x07\u0119\x02\x02\u031B\u0318\x03\x02\x02\x02\u031B" +
		"\u031C\x03\x02\x02\x02\u031C\u041D\x03\x02\x02\x02\u031D\u031E\x07\xD4" +
		"\x02\x02\u031E\u031F\x07\xE0\x02\x02\u031F\u0322\x07W\x02\x02\u0320\u0321" +
		"\t\x05\x02\x02\u0321\u0323\x05\xB2Z\x02\u0322\u0320\x03\x02\x02\x02\u0322" +
		"\u0323\x03\x02\x02\x02\u0323\u0324\x03\x02\x02\x02\u0324\u0325\x07\x84" +
		"\x02\x02\u0325\u0327\x07\u0119\x02\x02\u0326\u0328\x05*\x16\x02\u0327" +
		"\u0326\x03\x02\x02\x02\u0327\u0328\x03\x02\x02\x02\u0328\u041D\x03\x02" +
		"\x02\x02\u0329\u032A\x07\xD4\x02\x02\u032A\u032B\x07\xE3\x02\x02\u032B" +
		"\u0330\x05\xB2Z\x02\u032C\u032D\x07\x03\x02\x02\u032D\u032E\x05@!\x02" +
		"\u032E\u032F\x07\x04\x02\x02\u032F\u0331\x03\x02\x02\x02\u0330\u032C\x03" +
		"\x02\x02\x02\u0330\u0331\x03\x02\x02\x02\u0331\u041D\x03\x02\x02\x02\u0332" +
		"\u0333\x07\xD4\x02\x02\u0333\u0334\x07-\x02\x02\u0334\u0335\t\x05\x02" +
		"\x02\u0335\u0338\x05\xB2Z\x02\u0336\u0337\t\x05\x02\x02\u0337\u0339\x05" +
		"\xB2Z\x02\u0338\u0336\x03\x02\x02\x02\u0338\u0339\x03\x02\x02\x02\u0339" +
		"\u041D\x03\x02\x02\x02\u033A\u033B\x07\xD4\x02\x02\u033B\u033E\x07\u0100" +
		"\x02\x02\u033C\u033D\t\x05\x02\x02\u033D\u033F\x05\xB2Z\x02\u033E\u033C" +
		"\x03\x02\x02\x02\u033E\u033F\x03\x02\x02\x02\u033F\u0344\x03\x02\x02\x02" +
		"\u0340\u0342\x07\x84\x02\x02\u0341\u0340\x03\x02\x02\x02\u0341\u0342\x03" +
		"\x02\x02\x02\u0342\u0343\x03\x02\x02\x02\u0343\u0345\x07\u0119\x02\x02" +
		"\u0344\u0341\x03\x02\x02\x02\u0344\u0345\x03\x02\x02\x02\u0345\u041D\x03" +
		"\x02\x02\x02\u0346\u0347\x07\xD4\x02\x02\u0347\u0348\x07\xAA\x02\x02\u0348" +
		"\u034A\x05\xB2Z\x02\u0349\u034B\x05*\x16\x02\u034A\u0349\x03\x02\x02\x02" +
		"\u034A\u034B\x03\x02\x02\x02\u034B\u041D\x03\x02\x02\x02\u034C\u034E\x07" +
		"\xD4\x02\x02\u034D\u034F\x05\u0106\x84\x02\u034E\u034D\x03\x02\x02\x02" +
		"\u034E\u034F\x03\x02\x02\x02\u034F\u0350\x03\x02\x02\x02\u0350\u0358\x07" +
		"h\x02\x02\u0351\u0353\x07\x84\x02\x02\u0352\u0351\x03\x02\x02\x02\u0352" +
		"\u0353\x03\x02\x02\x02\u0353\u0356\x03\x02\x02\x02\u0354\u0357\x05\xB2" +
		"Z\x02\u0355\u0357\x07\u0119\x02\x02\u0356\u0354\x03\x02\x02\x02\u0356" +
		"\u0355\x03\x02\x02\x02\u0357\u0359\x03\x02\x02\x02\u0358\u0352\x03\x02" +
		"\x02\x02\u0358\u0359\x03\x02\x02\x02\u0359\u041D\x03\x02\x02\x02\u035A" +
		"\u035B\x07\xD4\x02\x02\u035B\u035C\x076\x02\x02\u035C\u035D\x07\xE0\x02" +
		"\x02\u035D\u0360\x05\xB2Z\x02\u035E\u035F\x07\x17\x02\x02\u035F\u0361" +
		"\x07\xCE\x02\x02\u0360\u035E\x03\x02\x02\x02\u0360\u0361\x03\x02\x02\x02" +
		"\u0361\u041D\x03\x02\x02\x02\u0362\u0363\x07\xD4\x02\x02\u0363\u0364\x07" +
		"9\x02\x02\u0364\u041D\x07\x93\x02\x02\u0365\u0366\t\n\x02\x02\u0366\u0368" +
		"\x07g\x02\x02\u0367\u0369\x07W\x02\x02\u0368\u0367\x03\x02\x02\x02\u0368" +
		"\u0369\x03\x02\x02\x02\u0369\u036A\x03\x02\x02\x02\u036A\u041D\x050\x19" +
		"\x02\u036B\u036C\t\n\x02\x02\u036C\u036E\x05.\x18\x02\u036D\u036F\x07" +
		"W\x02\x02\u036E\u036D\x03\x02\x02\x02\u036E\u036F\x03\x02\x02\x02\u036F" +
		"\u0370\x03\x02\x02\x02\u0370\u0371\x05\xB2Z\x02\u0371\u041D\x03\x02\x02" +
		"\x02\u0372\u0374\t\n\x02\x02\u0373\u0375\x07\xE0\x02\x02\u0374\u0373\x03" +
		"\x02\x02\x02\u0374\u0375\x03\x02\x02\x02\u0375\u0377\x03\x02\x02\x02\u0376" +
		"\u0378\t\v\x02\x02\u0377\u0376\x03\x02\x02\x02\u0377\u0378\x03\x02\x02" +
		"\x02\u0378\u0379\x03\x02\x02\x02\u0379\u037B\x05\xB2Z\x02\u037A\u037C" +
		"\x05*\x16\x02\u037B\u037A\x03\x02\x02\x02\u037B\u037C\x03\x02\x02\x02" +
		"\u037C\u037E\x03\x02\x02\x02\u037D\u037F\x052\x1A\x02\u037E\u037D\x03" +
		"\x02\x02\x02\u037E\u037F\x03\x02\x02\x02\u037F\u041D\x03\x02\x02\x02\u0380" +
		"\u0382\t\n\x02\x02\u0381\u0383\x07\xB4\x02\x02\u0382\u0381\x03\x02\x02" +
		"\x02\u0382\u0383\x03\x02\x02\x02\u0383\u0384\x03\x02\x02\x02\u0384\u041D" +
		"\x05$\x13\x02\u0385\u0386\x07.\x02\x02\u0386\u0387\x07\x9B\x02\x02\u0387" +
		"\u0388\x05.\x18\x02\u0388\u0389\x05\xB2Z\x02\u0389\u038A\x07{\x02\x02" +
		"\u038A\u038B\t\f\x02\x02\u038B\u041D\x03\x02\x02\x02\u038C\u038D\x07." +
		"\x02\x02\u038D\u038E\x07\x9B\x02\x02\u038E\u038F\x07\xE0\x02\x02\u038F" +
		"\u0390\x05\xB2Z\x02\u0390\u0391\x07{\x02\x02\u0391\u0392\t\f\x02\x02\u0392" +
		"\u041D\x03\x02\x02\x02\u0393\u0394\x07\xBB\x02\x02\u0394\u0395\x07\xE0" +
		"\x02\x02\u0395\u041D\x05\xB2Z\x02\u0396\u0397\x07\xBB\x02\x02\u0397\u0398" +
		"\x07g\x02\x02\u0398\u041D\x05\xB2Z\x02\u0399\u03A1\x07\xBB\x02\x02\u039A" +
		"\u03A2\x07\u0119\x02\x02\u039B\u039D\v\x02\x02\x02\u039C\u039B\x03\x02" +
		"\x02\x02\u039D\u03A0\x03\x02\x02\x02\u039E\u039F\x03\x02\x02\x02\u039E" +
		"\u039C\x03\x02\x02\x02\u039F\u03A2\x03\x02\x02\x02\u03A0\u039E\x03\x02" +
		"\x02\x02\u03A1\u039A\x03\x02\x02\x02\u03A1\u039E\x03\x02\x02\x02\u03A2" +
		"\u041D\x03\x02\x02\x02\u03A3\u03A5\x07 \x02\x02\u03A4\u03A6\x07\x81\x02" +
		"\x02\u03A5\u03A4\x03\x02\x02\x02\u03A5\u03A6\x03\x02\x02\x02\u03A6\u03A7" +
		"\x03\x02\x02\x02\u03A7\u03A8\x07\xE0\x02\x02\u03A8\u03AB\x05\xB2Z\x02" +
		"\u03A9\u03AA\x07\x9E\x02\x02\u03AA\u03AC\x05<\x1F\x02\u03AB\u03A9\x03" +
		"\x02\x02\x02\u03AB\u03AC\x03\x02\x02\x02\u03AC\u03B1\x03\x02\x02\x02\u03AD" +
		"\u03AF\x07\x17\x02\x02\u03AE\u03AD\x03\x02\x02\x02\u03AE\u03AF\x03\x02" +
		"\x02\x02\u03AF\u03B0\x03\x02\x02\x02\u03B0\u03B2\x05$\x13\x02\u03B1\u03AE" +
		"\x03\x02\x02\x02\u03B1\u03B2\x03\x02\x02\x02\u03B2\u041D\x03\x02\x02\x02" +
		"\u03B3\u03B4\x07\xF4\x02\x02\u03B4\u03B7\x07\xE0\x02\x02\u03B5\u03B6\x07" +
		"n\x02\x02\u03B6\u03B8\x07T\x02\x02\u03B7\u03B5\x03\x02\x02\x02\u03B7\u03B8" +
		"\x03\x02\x02\x02\u03B8\u03B9\x03\x02\x02\x02\u03B9\u041D\x05\xB2Z\x02" +
		"\u03BA\u03BB\x07&\x02\x02\u03BB\u041D\x07 \x02\x02\u03BC\u03BD\x07\x88" +
		"\x02\x02\u03BD\u03BF\x07>\x02\x02\u03BE\u03C0\x07\x89\x02\x02\u03BF\u03BE" +
		"\x03\x02\x02\x02\u03BF\u03C0\x03\x02\x02\x02\u03C0\u03C1\x03\x02\x02\x02" +
		"\u03C1\u03C2\x07u\x02\x02\u03C2\u03C4\x07\u0119\x02\x02\u03C3\u03C5\x07" +
		"\xA7\x02\x02\u03C4\u03C3\x03\x02\x02\x02\u03C4\u03C5\x03\x02\x02\x02\u03C5" +
		"\u03C6\x03\x02\x02\x02\u03C6\u03C7\x07z\x02\x02\u03C7\u03C8\x07\xE0\x02" +
		"\x02\u03C8\u03CA\x05\xB2Z\x02\u03C9\u03CB\x05*\x16\x02\u03CA\u03C9\x03" +
		"\x02\x02\x02\u03CA\u03CB\x03\x02\x02\x02\u03CB\u041D\x03\x02\x02\x02\u03CC" +
		"\u03CD\x07\xF0\x02\x02\u03CD\u03CE\x07\xE0\x02\x02\u03CE\u03D0\x05\xB2" +
		"Z\x02\u03CF\u03D1\x05*\x16\x02\u03D0\u03CF\x03\x02\x02\x02\u03D0\u03D1" +
		"\x03\x02\x02\x02\u03D1\u041D\x03\x02\x02\x02\u03D2\u03D3\x07\x92\x02\x02" +
		"\u03D3\u03D4\x07\xBD\x02\x02\u03D4\u03D5\x07\xE0\x02\x02\u03D5\u041D\x05" +
		"\xB2Z\x02\u03D6\u03D7\t\r\x02\x02\u03D7\u03DF\x05\u0106\x84\x02\u03D8" +
		"\u03E0\x07\u0119\x02\x02\u03D9\u03DB\v\x02\x02\x02\u03DA\u03D9\x03\x02" +
		"\x02\x02\u03DB\u03DE\x03\x02\x02\x02\u03DC\u03DD\x03\x02\x02\x02\u03DC" +
		"\u03DA\x03\x02\x02\x02\u03DD\u03E0\x03\x02\x02\x02\u03DE\u03DC\x03\x02" +
		"\x02\x02\u03DF\u03D8\x03\x02\x02\x02\u03DF\u03DC\x03\x02\x02\x02\u03E0" +
		"\u041D\x03\x02\x02\x02\u03E1\u03E2\x07\xD1\x02\x02\u03E2\u03E6\x07\xC4" +
		"\x02\x02\u03E3\u03E5\v\x02\x02\x02\u03E4\u03E3\x03\x02\x02\x02\u03E5\u03E8" +
		"\x03\x02\x02\x02\u03E6\u03E7\x03\x02\x02\x02\u03E6\u03E4\x03\x02\x02\x02" +
		"\u03E7\u041D\x03\x02\x02\x02\u03E8\u03E6\x03\x02\x02\x02\u03E9\u03EA\x07" +
		"\xD1\x02\x02\u03EA\u03EB\x07\xE7\x02\x02\u03EB\u03EC\x07\u0105\x02\x02" +
		"\u03EC\u041D\x05\xD4k\x02\u03ED\u03EE\x07\xD1\x02\x02\u03EE\u03EF\x07" +
		"\xE7\x02\x02\u03EF\u03F0\x07\u0105\x02\x02\u03F0\u041D\t\x0E\x02\x02\u03F1" +
		"\u03F2\x07\xD1\x02\x02\u03F2\u03F3\x07\xE7\x02\x02\u03F3\u03F7\x07\u0105" +
		"\x02\x02\u03F4\u03F6\v\x02\x02\x02\u03F5\u03F4\x03\x02\x02\x02\u03F6\u03F9" +
		"\x03\x02\x02\x02\u03F7\u03F8\x03\x02\x02\x02\u03F7\u03F5\x03\x02\x02\x02" +
		"\u03F8\u041D\x03\x02\x02\x02\u03F9\u03F7\x03\x02\x02\x02\u03FA\u03FB\x07" +
		"\xD1\x02\x02\u03FB\u0403\x05\x14\v\x02\u03FC\u0400\x07\u0106\x02\x02\u03FD" +
		"\u03FF\v\x02\x02\x02\u03FE\u03FD\x03\x02\x02\x02\u03FF\u0402\x03\x02\x02" +
		"\x02\u0400\u0401\x03\x02\x02\x02\u0400\u03FE\x03\x02\x02\x02\u0401\u0404" +
		"\x03\x02\x02\x02\u0402\u0400\x03\x02\x02\x02\u0403\u03FC\x03\x02\x02\x02" +
		"\u0403\u0404\x03\x02\x02\x02\u0404\u041D\x03\x02\x02\x02\u0405\u0409\x07" +
		"\xD1\x02\x02\u0406\u0408\v\x02\x02\x02\u0407\u0406\x03\x02\x02\x02\u0408" +
		"\u040B\x03\x02\x02\x02\u0409\u040A\x03\x02\x02\x02\u0409\u0407\x03\x02" +
		"\x02\x02\u040A\u041D\x03\x02\x02\x02\u040B\u0409\x03\x02\x02\x02\u040C" +
		"\u040D\x07\xBF\x02\x02\u040D\u041D\x05\x14\v\x02\u040E\u0412\x07\xBF\x02" +
		"\x02\u040F\u0411\v\x02\x02\x02\u0410\u040F\x03\x02\x02\x02\u0411\u0414" +
		"\x03\x02\x02\x02\u0412\u0413\x03\x02\x02\x02\u0412\u0410\x03\x02\x02\x02" +
		"\u0413\u041D\x03\x02\x02\x02\u0414\u0412\x03\x02\x02\x02\u0415\u0419\x05" +
		"\x16\f\x02\u0416\u0418\v\x02\x02\x02\u0417\u0416\x03\x02\x02\x02\u0418" +
		"\u041B\x03\x02\x02\x02\u0419\u041A\x03\x02\x02\x02\u0419\u0417\x03\x02" +
		"\x02\x02\u041A\u041D\x03\x02\x02\x02\u041B\u0419\x03\x02\x02\x02\u041C" +
		"\u0134\x03\x02\x02\x02\u041C\u0136\x03\x02\x02\x02\u041C\u0139\x03\x02" +
		"\x02\x02\u041C\u013E\x03\x02\x02\x02\u041C\u0150\x03\x02\x02\x02\u041C" +
		"\u0157\x03\x02\x02\x02\u041C\u015D\x03\x02\x02\x02\u041C\u0167\x03\x02" +
		"\x02\x02\u041C\u0173\x03\x02\x02\x02\u041C\u0182\x03\x02\x02\x02\u041C" +
		"\u01A7\x03\x02\x02\x02\u041C\u01BC\x03\x02\x02\x02\u041C\u01CB\x03\x02" +
		"\x02\x02\u041C\u01DC\x03\x02\x02\x02\u041C\u01E3\x03\x02\x02\x02\u041C" +
		"\u01EC\x03\x02\x02\x02\u041C\u01F5\x03\x02\x02\x02\u041C\u01FE\x03\x02" +
		"\x02\x02\u041C\u0205\x03\x02\x02\x02\u041C\u020C\x03\x02\x02\x02\u041C" +
		"\u0213\x03\x02\x02\x02\u041C\u021E\x03\x02\x02\x02\u041C\u0229\x03\x02" +
		"\x02\x02\u041C\u0238\x03\x02\x02\x02\u041C\u0244\x03\x02\x02\x02\u041C" +
		"\u0252\x03\x02\x02\x02\u041C\u025C\x03\x02\x02\x02\u041C\u026A\x03\x02" +
		"\x02\x02\u041C\u0272\x03\x02\x02\x02\u041C\u0285\x03\x02\x02\x02\u041C" +
		"\u028E\x03\x02\x02\x02\u041C\u0294\x03\x02\x02\x02\u041C\u029E\x03\x02" +
		"\x02\x02\u041C\u02A5\x03\x02\x02\x02\u041C\u02C8\x03\x02\x02\x02\u041C" +
		"\u02DE\x03\x02\x02\x02\u041C\u02E6\x03\x02\x02\x02\u041C\u0302\x03\x02" +
		"\x02\x02\u041C\u030C\x03\x02\x02\x02\u041C\u0311\x03\x02\x02\x02\u041C" +
		"\u031D\x03\x02\x02\x02\u041C\u0329\x03\x02\x02\x02\u041C\u0332\x03\x02" +
		"\x02\x02\u041C\u033A\x03\x02\x02\x02\u041C\u0346\x03\x02\x02\x02\u041C" +
		"\u034C\x03\x02\x02\x02\u041C\u035A\x03\x02\x02\x02\u041C\u0362\x03\x02" +
		"\x02\x02\u041C\u0365\x03\x02\x02\x02\u041C\u036B\x03\x02\x02\x02\u041C" +
		"\u0372\x03\x02\x02\x02\u041C\u0380\x03\x02\x02\x02\u041C\u0385\x03\x02" +
		"\x02\x02\u041C\u038C\x03\x02\x02\x02\u041C\u0393\x03\x02\x02\x02\u041C" +
		"\u0396\x03\x02\x02\x02\u041C\u0399\x03\x02\x02\x02\u041C\u03A3\x03\x02" +
		"\x02\x02\u041C\u03B3\x03\x02\x02\x02\u041C\u03BA\x03\x02\x02\x02\u041C" +
		"\u03BC\x03\x02\x02\x02\u041C\u03CC\x03\x02\x02\x02\u041C\u03D2\x03\x02" +
		"\x02\x02\u041C\u03D6\x03\x02\x02\x02\u041C\u03E1\x03\x02\x02\x02\u041C" +
		"\u03E9\x03\x02\x02\x02\u041C\u03ED\x03\x02\x02\x02\u041C\u03F1\x03\x02" +
		"\x02\x02\u041C\u03FA\x03\x02\x02\x02\u041C\u0405\x03\x02\x02\x02\u041C" +
		"\u040C\x03\x02\x02\x02\u041C\u040E\x03\x02\x02\x02\u041C\u0415\x03\x02" +
		"\x02\x02\u041D\x13\x03\x02\x02\x02\u041E\u041F\x05\u010A\x86\x02\u041F" +
		"\x15\x03\x02\x02\x02\u0420\u0421\x076\x02\x02\u0421\u04C9\x07\xC4\x02" +
		"\x02\u0422\u0423\x07M\x02\x02\u0423\u04C9\x07\xC4\x02\x02\u0424\u0426" +
		"\x07j\x02\x02\u0425\u0427\x07\xC4\x02\x02\u0426\u0425\x03\x02\x02\x02" +
		"\u0426\u0427\x03\x02\x02\x02\u0427\u04C9\x03\x02\x02\x02\u0428\u042A\x07" +
		"\xC1\x02\x02\u0429\u042B\x07\xC4\x02\x02\u042A\u0429\x03\x02\x02\x02\u042A" +
		"\u042B\x03\x02\x02\x02\u042B\u04C9\x03\x02\x02\x02\u042C\u042D\x07\xD4" +
		"\x02\x02\u042D\u04C9\x07j\x02\x02\u042E\u042F\x07\xD4\x02\x02\u042F\u0431" +
		"\x07\xC4\x02\x02\u0430\u0432\x07j\x02\x02\u0431\u0430\x03\x02\x02\x02" +
		"\u0431\u0432\x03\x02\x02\x02\u0432\u04C9\x03\x02\x02\x02\u0433\u0434\x07" +
		"\xD4\x02\x02\u0434\u04C9\x07\xB1\x02\x02\u0435\u0436\x07\xD4\x02\x02\u0436" +
		"\u04C9\x07\xC5\x02\x02\u0437\u0438\x07\xD4\x02\x02\u0438\u0439\x079\x02" +
		"\x02\u0439\u04C9\x07\xC5\x02\x02\u043A\u043B\x07V\x02\x02\u043B\u04C9" +
		"\x07\xE0\x02\x02\u043C\u043D\x07p\x02\x02\u043D\u04C9\x07\xE0\x02\x02" +
		"\u043E\u043F\x07\xD4\x02\x02\u043F\u04C9\x071\x02\x02\u0440\u0441\x07" +
		"\xD4\x02\x02\u0441\u0442\x076\x02\x02\u0442\u04C9\x07\xE0\x02\x02\u0443" +
		"\u0444\x07\xD4\x02\x02\u0444\u04C9\x07\xEC\x02\x02\u0445\u0446\x07\xD4" +
		"\x02\x02\u0446\u04C9\x07s\x02\x02\u0447\u0448\x07\xD4\x02\x02\u0448\u04C9" +
		"\x07\x8C\x02\x02\u0449\u044A\x076\x02\x02\u044A\u04C9\x07r\x02\x02\u044B" +
		"\u044C\x07M\x02\x02\u044C\u04C9\x07r\x02\x02\u044D\u044E\x07\x10\x02\x02" +
		"\u044E\u04C9\x07r\x02\x02\u044F\u0450\x07\x8B\x02\x02\u0450\u04C9\x07" +
		"\xE0\x02\x02\u0451\u0452\x07\x8B\x02\x02\u0452\u04C9\x07?\x02\x02\u0453" +
		"\u0454\x07\xF8\x02\x02\u0454\u04C9\x07\xE0\x02\x02\u0455\u0456\x07\xF8" +
		"\x02\x02\u0456\u04C9\x07?\x02\x02\u0457\u0458\x076\x02\x02\u0458\u0459" +
		"\x07\xE4\x02\x02\u0459\u04C9\x07\x8E\x02\x02\u045A\u045B\x07M\x02\x02" +
		"\u045B\u045C\x07\xE4\x02\x02\u045C\u04C9\x07\x8E\x02\x02\u045D\u045E\x07" +
		"\x10\x02\x02\u045E\u045F\x07\xE0\x02\x02\u045F\u0460\x05\xB4[\x02\u0460" +
		"\u0461\x07\x97\x02\x02\u0461\u0462\x07(\x02\x02\u0462\u04C9\x03\x02\x02" +
		"\x02\u0463\u0464\x07\x10\x02\x02\u0464\u0465\x07\xE0\x02\x02\u0465\u0466" +
		"\x05\xB4[\x02\u0466\u0467\x07(\x02\x02\u0467\u0468\x07\x1F\x02\x02\u0468" +
		"\u04C9\x03\x02\x02\x02\u0469\u046A\x07\x10\x02\x02\u046A\u046B\x07\xE0" +
		"\x02\x02\u046B\u046C\x05\xB4[\x02\u046C\u046D\x07\x97\x02\x02\u046D\u046E" +
		"\x07\xD8\x02\x02\u046E\u04C9\x03\x02\x02\x02\u046F\u0470\x07\x10\x02\x02" +
		"\u0470\u0471\x07\xE0\x02\x02\u0471\u0472\x05\xB4[\x02\u0472\u0473\x07" +
		"\xD5\x02\x02\u0473\u0474\x07\x1F\x02\x02\u0474\u04C9\x03\x02\x02\x02\u0475" +
		"\u0476\x07\x10\x02\x02\u0476\u0477\x07\xE0\x02\x02\u0477\u0478\x05\xB4" +
		"[\x02\u0478\u0479\x07\x97\x02\x02\u0479\u047A\x07\xD5\x02\x02\u047A\u04C9" +
		"\x03\x02\x02\x02\u047B\u047C\x07\x10\x02\x02\u047C\u047D\x07\xE0\x02\x02" +
		"\u047D\u047E\x05\xB4[\x02\u047E\u047F\x07\x97\x02\x02\u047F\u0480\x07" +
		"\xDB\x02\x02\u0480\u0481\x07\x17\x02\x02\u0481\u0482\x07H\x02\x02\u0482" +
		"\u04C9\x03\x02\x02\x02\u0483\u0484\x07\x10\x02\x02\u0484\u0485\x07\xE0" +
		"\x02\x02\u0485\u0486\x05\xB4[\x02\u0486\u0487\x07\xD1\x02\x02\u0487\u0488" +
		"\x07\xD5\x02\x02\u0488\u0489\x07\x8A\x02\x02\u0489\u04C9\x03\x02\x02\x02" +
		"\u048A\u048B\x07\x10\x02\x02\u048B\u048C\x07\xE0\x02\x02\u048C\u048D\x05" +
		"\xB4[\x02\u048D\u048E\x07S\x02\x02\u048E\u048F\x07\xA8\x02\x02\u048F\u04C9" +
		"\x03\x02\x02\x02\u0490\u0491\x07\x10\x02\x02\u0491\u0492\x07\xE0\x02\x02" +
		"\u0492\u0493\x05\xB4[\x02\u0493\u0494\x07\x15\x02\x02\u0494\u0495\x07" +
		"\xA8\x02\x02\u0495\u04C9\x03\x02\x02\x02\u0496\u0497\x07\x10\x02\x02\u0497" +
		"\u0498\x07\xE0\x02\x02\u0498\u0499\x05\xB4[\x02\u0499\u049A\x07\xF2\x02" +
		"\x02\u049A\u049B\x07\xA8\x02\x02\u049B\u04C9\x03\x02\x02\x02\u049C\u049D" +
		"\x07\x10\x02\x02\u049D\u049E\x07\xE0\x02\x02\u049E\u049F\x05\xB4[\x02" +
		"\u049F\u04A0\x07\xE9\x02\x02\u04A0\u04C9\x03\x02\x02\x02\u04A1\u04A2\x07" +
		"\x10\x02\x02\u04A2\u04A3\x07\xE0\x02\x02\u04A3\u04A5\x05\xB4[\x02\u04A4" +
		"\u04A6\x05*\x16\x02\u04A5\u04A4\x03\x02\x02\x02\u04A5\u04A6\x03\x02\x02" +
		"\x02\u04A6\u04A7\x03\x02\x02\x02\u04A7\u04A8\x070\x02\x02\u04A8\u04C9" +
		"\x03\x02\x02\x02\u04A9\u04AA\x07\x10\x02\x02\u04AA\u04AB\x07\xE0\x02\x02" +
		"\u04AB\u04AD\x05\xB4[\x02\u04AC\u04AE\x05*\x16\x02\u04AD\u04AC\x03\x02" +
		"\x02\x02\u04AD\u04AE\x03\x02\x02\x02\u04AE\u04AF\x03\x02\x02\x02\u04AF" +
		"\u04B0\x073\x02\x02\u04B0\u04C9\x03\x02\x02\x02\u04B1\u04B2\x07\x10\x02" +
		"\x02\u04B2\u04B3\x07\xE0\x02\x02\u04B3\u04B5\x05\xB4[\x02\u04B4\u04B6" +
		"\x05*\x16\x02\u04B5\u04B4\x03\x02\x02\x02\u04B5\u04B6\x03\x02\x02\x02" +
		"\u04B6\u04B7\x03\x02\x02\x02\u04B7\u04B8\x07\xD1\x02\x02\u04B8\u04B9\x07" +
		"^\x02\x02\u04B9\u04C9\x03\x02\x02\x02\u04BA\u04BB\x07\x10\x02\x02\u04BB" +
		"\u04BC\x07\xE0\x02\x02\u04BC\u04BE\x05\xB4[\x02\u04BD\u04BF\x05*\x16\x02" +
		"\u04BE\u04BD\x03\x02\x02\x02\u04BE\u04BF\x03\x02\x02\x02\u04BF\u04C0\x03" +
		"\x02\x02\x02\u04C0\u04C1\x07\xBE\x02\x02\u04C1\u04C2\x07-\x02\x02\u04C2" +
		"\u04C9\x03\x02\x02\x02\u04C3\u04C4\x07\xD9\x02\x02\u04C4\u04C9\x07\xEB" +
		"\x02\x02\u04C5\u04C9\x07/\x02\x02\u04C6\u04C9\x07\xC6\x02\x02\u04C7\u04C9" +
		"\x07G\x02\x02\u04C8\u0420";
	private static readonly _serializedATNSegment3: string =
		"\x03\x02\x02\x02\u04C8\u0422\x03\x02\x02\x02\u04C8\u0424\x03\x02\x02\x02" +
		"\u04C8\u0428\x03\x02\x02\x02\u04C8\u042C\x03\x02\x02\x02\u04C8\u042E\x03" +
		"\x02\x02\x02\u04C8\u0433\x03\x02\x02\x02\u04C8\u0435\x03\x02\x02\x02\u04C8" +
		"\u0437\x03\x02\x02\x02\u04C8\u043A\x03\x02\x02\x02\u04C8\u043C\x03\x02" +
		"\x02\x02\u04C8\u043E\x03\x02\x02\x02\u04C8\u0440\x03\x02\x02\x02\u04C8" +
		"\u0443\x03\x02\x02\x02\u04C8\u0445\x03\x02\x02\x02\u04C8\u0447\x03\x02" +
		"\x02\x02\u04C8\u0449\x03\x02\x02\x02\u04C8\u044B\x03\x02\x02\x02\u04C8" +
		"\u044D\x03\x02\x02\x02\u04C8\u044F\x03\x02\x02\x02\u04C8\u0451\x03\x02" +
		"\x02\x02\u04C8\u0453\x03\x02\x02\x02\u04C8\u0455\x03\x02\x02\x02\u04C8" +
		"\u0457\x03\x02\x02\x02\u04C8\u045A\x03\x02\x02\x02\u04C8\u045D\x03\x02" +
		"\x02\x02\u04C8\u0463\x03\x02\x02\x02\u04C8\u0469\x03\x02\x02\x02\u04C8" +
		"\u046F\x03\x02\x02\x02\u04C8\u0475\x03\x02\x02\x02\u04C8\u047B\x03\x02" +
		"\x02\x02\u04C8\u0483\x03\x02\x02\x02\u04C8\u048A\x03\x02\x02\x02\u04C8" +
		"\u0490\x03\x02\x02\x02\u04C8\u0496\x03\x02\x02\x02\u04C8\u049C\x03\x02" +
		"\x02\x02\u04C8\u04A1\x03\x02\x02\x02\u04C8\u04A9\x03\x02\x02\x02\u04C8" +
		"\u04B1\x03\x02\x02\x02\u04C8\u04BA\x03\x02\x02\x02\u04C8\u04C3\x03\x02" +
		"\x02\x02\u04C8\u04C5\x03\x02\x02\x02\u04C8\u04C6\x03\x02\x02\x02\u04C8" +
		"\u04C7\x03\x02\x02\x02\u04C9\x17\x03\x02\x02\x02\u04CA\u04CC\x076\x02" +
		"\x02\u04CB\u04CD\x07\xE4\x02\x02\u04CC\u04CB\x03\x02\x02\x02\u04CC\u04CD" +
		"\x03\x02\x02\x02\u04CD\u04CF\x03\x02\x02\x02\u04CE\u04D0\x07X\x02\x02" +
		"\u04CF\u04CE\x03\x02\x02\x02\u04CF\u04D0\x03\x02\x02\x02\u04D0\u04D1\x03" +
		"\x02\x02\x02\u04D1\u04D5\x07\xE0\x02\x02\u04D2\u04D3\x07n\x02\x02\u04D3" +
		"\u04D4\x07\x97\x02\x02\u04D4\u04D6\x07T\x02\x02\u04D5\u04D2\x03\x02\x02" +
		"\x02\u04D5\u04D6\x03\x02\x02\x02\u04D6\u04D7\x03\x02\x02\x02\u04D7\u04D8" +
		"\x05\xB2Z\x02\u04D8\x19\x03\x02\x02\x02\u04D9\u04DA\x076\x02\x02\u04DA" +
		"\u04DC\x07\x9F\x02\x02\u04DB\u04D9\x03\x02\x02\x02\u04DB\u04DC\x03\x02" +
		"\x02\x02\u04DC\u04DD\x03\x02\x02\x02\u04DD\u04DE\x07\xBE\x02\x02\u04DE" +
		"\u04DF\x07\xE0\x02\x02\u04DF\u04E0\x05\xB2Z\x02\u04E0\x1B\x03\x02\x02" +
		"\x02\u04E1\u04E2\x07(\x02\x02\u04E2\u04E3\x07\x1F\x02\x02\u04E3\u04E7" +
		"\x05\x9AN\x02\u04E4\u04E5\x07\xD8\x02\x02\u04E5\u04E6\x07\x1F\x02\x02" +
		"\u04E6\u04E8\x05\x9EP\x02\u04E7\u04E4\x03\x02\x02\x02\u04E7\u04E8\x03" +
		"\x02\x02\x02\u04E8\u04E9\x03\x02\x02\x02\u04E9\u04EA\x07z\x02\x02\u04EA" +
		"\u04EB\x07\u011D\x02\x02\u04EB\u04EC\x07\x1E\x02\x02\u04EC\x1D\x03\x02" +
		"\x02\x02\u04ED\u04EE\x07\xD5\x02\x02\u04EE\u04EF\x07\x1F\x02\x02\u04EF" +
		"\u04F0\x05\x9AN\x02\u04F0\u04F3\x07\x9B\x02\x02\u04F1\u04F4\x05D#\x02" +
		"\u04F2\u04F4\x05F$\x02\u04F3\u04F1\x03\x02\x02\x02\u04F3\u04F2\x03\x02" +
		"\x02\x02\u04F4\u04F8\x03\x02\x02\x02\u04F5\u04F6\x07\xDB\x02\x02\u04F6" +
		"\u04F7\x07\x17\x02\x02\u04F7\u04F9\x07H\x02\x02\u04F8\u04F5\x03\x02\x02" +
		"\x02\u04F8\u04F9\x03\x02\x02\x02\u04F9\x1F\x03\x02\x02\x02\u04FA\u04FB" +
		"\x07\x8A\x02\x02\u04FB\u04FC\x07\u0119\x02\x02\u04FC!\x03\x02\x02\x02" +
		"\u04FD\u04FE\x07.\x02\x02\u04FE\u04FF\x07\u0119\x02\x02\u04FF#\x03\x02" +
		"\x02\x02\u0500\u0502\x054\x1B\x02\u0501\u0500\x03\x02\x02\x02\u0501\u0502" +
		"\x03\x02\x02\x02\u0502\u0503\x03\x02\x02\x02\u0503\u0504\x05V,\x02\u0504" +
		"\u0505\x05R*\x02\u0505%\x03\x02\x02\x02\u0506\u0507\x07w\x02\x02\u0507" +
		"\u0509\x07\xA7\x02\x02\u0508\u050A\x07\xE0\x02\x02\u0509\u0508\x03\x02" +
		"\x02\x02\u0509\u050A\x03\x02\x02\x02\u050A\u050B\x03\x02\x02\x02\u050B" +
		"\u0512\x05\xB2Z\x02\u050C\u0510\x05*\x16\x02\u050D\u050E\x07n\x02\x02" +
		"\u050E\u050F\x07\x97\x02\x02\u050F\u0511\x07T\x02\x02\u0510\u050D\x03" +
		"\x02\x02\x02\u0510\u0511\x03\x02\x02\x02\u0511\u0513\x03\x02\x02\x02\u0512" +
		"\u050C\x03\x02\x02\x02\u0512\u0513\x03\x02\x02\x02\u0513\u053E\x03\x02" +
		"\x02\x02\u0514\u0515\x07w\x02\x02\u0515\u0517\x07z\x02\x02\u0516\u0518" +
		"\x07\xE0\x02\x02\u0517\u0516\x03\x02\x02\x02\u0517\u0518\x03\x02\x02\x02" +
		"\u0518\u0519\x03\x02\x02\x02\u0519\u051B\x05\xB2Z\x02\u051A\u051C\x05" +
		"*\x16\x02\u051B\u051A\x03\x02\x02\x02\u051B\u051C\x03\x02\x02\x02\u051C" +
		"\u0520\x03\x02\x02\x02\u051D\u051E\x07n\x02\x02\u051E\u051F\x07\x97\x02" +
		"\x02\u051F\u0521\x07T\x02\x02\u0520\u051D\x03\x02\x02\x02\u0520\u0521" +
		"\x03\x02\x02\x02\u0521\u053E\x03\x02\x02\x02\u0522\u0523\x07w\x02\x02" +
		"\u0523\u0525\x07\xA7\x02\x02\u0524\u0526\x07\x89\x02\x02\u0525\u0524\x03" +
		"\x02\x02\x02\u0525\u0526\x03\x02\x02\x02\u0526\u0527\x03\x02\x02\x02\u0527" +
		"\u0528\x07I\x02\x02\u0528\u052A\x07\u0119\x02\x02\u0529\u052B\x05\xAE" +
		"X\x02\u052A\u0529\x03\x02\x02\x02\u052A\u052B\x03\x02\x02\x02\u052B\u052D" +
		"\x03\x02\x02\x02\u052C\u052E\x05H%\x02\u052D\u052C\x03\x02\x02\x02\u052D" +
		"\u052E\x03\x02\x02\x02\u052E\u053E\x03\x02\x02\x02\u052F\u0530\x07w\x02" +
		"\x02\u0530\u0532\x07\xA7\x02\x02\u0531\u0533\x07\x89\x02\x02\u0532\u0531" +
		"\x03\x02\x02\x02\u0532\u0533\x03\x02\x02\x02\u0533\u0534\x03\x02\x02\x02" +
		"\u0534\u0536\x07I\x02\x02\u0535\u0537\x07\u0119\x02\x02\u0536\u0535\x03" +
		"\x02\x02\x02\u0536\u0537\x03\x02\x02\x02\u0537\u0538\x03\x02\x02\x02\u0538" +
		"\u053B\x058\x1D\x02\u0539\u053A\x07\x9E\x02\x02\u053A\u053C\x05<\x1F\x02" +
		"\u053B\u0539\x03\x02\x02\x02\u053B\u053C\x03\x02\x02\x02\u053C\u053E\x03" +
		"\x02\x02\x02\u053D\u0506\x03\x02\x02\x02\u053D\u0514\x03\x02\x02\x02\u053D" +
		"\u0522\x03\x02\x02\x02\u053D\u052F\x03\x02\x02\x02\u053E\'\x03\x02\x02" +
		"\x02\u053F\u0541\x05*\x16\x02\u0540\u0542\x05 \x11\x02\u0541\u0540\x03" +
		"\x02\x02\x02\u0541\u0542\x03\x02\x02\x02\u0542)\x03\x02\x02\x02\u0543" +
		"\u0544\x07\xA8\x02\x02\u0544\u0545\x07\x03\x02\x02\u0545\u054A\x05,\x17" +
		"\x02\u0546\u0547\x07\x05\x02\x02\u0547\u0549\x05,\x17\x02\u0548\u0546" +
		"\x03\x02\x02\x02\u0549\u054C\x03\x02\x02\x02\u054A\u0548\x03\x02\x02\x02" +
		"\u054A\u054B\x03\x02\x02\x02\u054B\u054D\x03\x02\x02\x02\u054C\u054A\x03" +
		"\x02\x02\x02\u054D\u054E\x07\x04\x02\x02\u054E+\x03\x02\x02\x02\u054F" +
		"\u0552\x05\u0106\x84\x02\u0550\u0551\x07\u0106\x02\x02\u0551\u0553\x05" +
		"\xCAf\x02\u0552\u0550\x03\x02\x02\x02\u0552\u0553\x03\x02\x02\x02\u0553" +
		"-\x03\x02\x02\x02\u0554\u0555\t\x0F\x02\x02\u0555/\x03\x02\x02\x02\u0556" +
		"\u055C\x05\u0100\x81\x02\u0557\u055C\x07\u0119\x02\x02\u0558\u055C\x05" +
		"\xCCg\x02\u0559\u055C\x05\xCEh\x02\u055A\u055C\x05\xD0i\x02\u055B\u0556" +
		"\x03\x02\x02\x02\u055B\u0557\x03\x02\x02\x02\u055B\u0558\x03\x02\x02\x02" +
		"\u055B\u0559\x03\x02\x02\x02\u055B\u055A\x03\x02\x02\x02\u055C1\x03\x02" +
		"\x02\x02\u055D\u0562\x05\u0106\x84\x02\u055E\u055F\x07\x06\x02\x02\u055F" +
		"\u0561\x05\u0106\x84\x02\u0560\u055E\x03\x02\x02\x02\u0561\u0564\x03\x02" +
		"\x02\x02\u0562\u0560\x03\x02\x02\x02\u0562\u0563\x03\x02\x02\x02\u0563" +
		"3\x03\x02\x02\x02\u0564\u0562\x03\x02\x02\x02\u0565\u0566\x07\u0104\x02" +
		"\x02\u0566\u056B\x056\x1C\x02\u0567\u0568\x07\x05\x02\x02\u0568\u056A" +
		"\x056\x1C\x02\u0569\u0567\x03\x02\x02\x02\u056A\u056D\x03\x02\x02\x02" +
		"\u056B\u0569\x03\x02\x02\x02\u056B\u056C\x03\x02\x02\x02\u056C5\x03\x02" +
		"\x02\x02\u056D\u056B\x03\x02\x02\x02\u056E\u0570\x05\u0102\x82\x02\u056F" +
		"\u0571\x05\x9AN\x02\u0570\u056F\x03\x02\x02\x02\u0570\u0571\x03\x02\x02" +
		"\x02\u0571\u0573\x03\x02\x02\x02\u0572\u0574\x07\x17\x02\x02\u0573\u0572" +
		"\x03\x02\x02\x02\u0573\u0574\x03\x02\x02\x02\u0574\u0575\x03\x02\x02\x02" +
		"\u0575\u0576\x07\x03\x02\x02\u0576\u0577\x05$\x13\x02\u0577\u0578\x07" +
		"\x04\x02\x02\u05787\x03\x02\x02\x02\u0579\u057A\x07\xFD\x02\x02\u057A" +
		"\u057B\x05\xB2Z\x02\u057B9\x03\x02\x02\x02\u057C\u057D\x07\x9E\x02\x02" +
		"\u057D\u0587\x05<\x1F\x02\u057E\u057F\x07\xA9\x02\x02\u057F\u0580\x07" +
		"\x1F\x02\x02\u0580\u0587\x05\xBA^\x02\u0581\u0587\x05\x1C\x0F\x02\u0582" +
		"\u0587\x05 \x11\x02\u0583\u0587\x05\"\x12\x02\u0584\u0585\x07\xE3\x02" +
		"\x02\u0585\u0587\x05<\x1F\x02\u0586\u057C\x03\x02\x02\x02\u0586\u057E" +
		"\x03\x02\x02\x02\u0586\u0581\x03\x02\x02\x02\u0586\u0582\x03\x02\x02\x02" +
		"\u0586\u0583\x03\x02\x02\x02\u0586\u0584\x03\x02\x02\x02\u0587\u058A\x03" +
		"\x02\x02\x02\u0588\u0586\x03\x02\x02\x02\u0588\u0589\x03\x02\x02\x02\u0589" +
		";\x03\x02\x02\x02\u058A\u0588\x03\x02\x02\x02\u058B\u058C\x07\x03\x02" +
		"\x02\u058C\u0591\x05> \x02\u058D\u058E\x07\x05\x02\x02\u058E\u0590\x05" +
		"> \x02\u058F\u058D\x03\x02\x02\x02\u0590\u0593\x03\x02\x02\x02\u0591\u058F" +
		"\x03\x02\x02\x02\u0591\u0592\x03\x02\x02\x02\u0592\u0594\x03\x02\x02\x02" +
		"\u0593\u0591\x03\x02\x02\x02\u0594\u0595\x07\x04\x02\x02\u0595=\x03\x02" +
		"\x02\x02\u0596\u059B\x05@!\x02\u0597\u0599\x07\u0106\x02\x02\u0598\u0597" +
		"\x03\x02\x02\x02\u0598\u0599\x03\x02\x02\x02\u0599\u059A\x03\x02\x02\x02" +
		"\u059A\u059C\x05B\"\x02\u059B\u0598\x03\x02\x02\x02\u059B\u059C\x03\x02" +
		"\x02\x02\u059C?\x03\x02\x02\x02\u059D\u05A2\x05\u0106\x84\x02\u059E\u059F" +
		"\x07\x06\x02\x02\u059F\u05A1\x05\u0106\x84\x02\u05A0\u059E\x03\x02\x02" +
		"\x02\u05A1\u05A4\x03\x02\x02\x02\u05A2\u05A0\x03\x02\x02\x02\u05A2\u05A3" +
		"\x03\x02\x02\x02\u05A3\u05A7\x03\x02\x02\x02\u05A4\u05A2\x03\x02\x02\x02" +
		"\u05A5\u05A7\x07\u0119\x02\x02\u05A6\u059D\x03\x02\x02\x02\u05A6\u05A5" +
		"\x03\x02\x02\x02\u05A7A\x03\x02\x02\x02\u05A8\u05AD\x07\u011D\x02\x02" +
		"\u05A9\u05AD\x07\u011F\x02\x02\u05AA\u05AD\x05\xD2j\x02\u05AB\u05AD\x07" +
		"\u0119\x02\x02\u05AC\u05A8\x03\x02\x02\x02\u05AC\u05A9\x03\x02\x02\x02" +
		"\u05AC\u05AA\x03\x02\x02\x02\u05AC\u05AB\x03\x02\x02\x02\u05ADC\x03\x02" +
		"\x02\x02\u05AE\u05AF\x07\x03\x02\x02\u05AF\u05B4\x05\xCAf\x02\u05B0\u05B1" +
		"\x07\x05\x02\x02\u05B1\u05B3\x05\xCAf\x02\u05B2\u05B0\x03\x02\x02\x02" +
		"\u05B3\u05B6\x03\x02\x02\x02\u05B4\u05B2\x03\x02\x02\x02\u05B4\u05B5\x03" +
		"\x02\x02\x02\u05B5\u05B7\x03\x02\x02\x02\u05B6\u05B4\x03\x02\x02\x02\u05B7" +
		"\u05B8\x07\x04\x02\x02\u05B8E\x03\x02\x02\x02\u05B9\u05BA\x07\x03\x02" +
		"\x02\u05BA\u05BF\x05D#\x02\u05BB\u05BC\x07\x05\x02\x02\u05BC\u05BE\x05" +
		"D#\x02\u05BD\u05BB\x03\x02\x02\x02\u05BE\u05C1\x03\x02\x02\x02\u05BF\u05BD" +
		"\x03\x02\x02\x02\u05BF\u05C0\x03\x02\x02\x02\u05C0\u05C2\x03\x02\x02\x02" +
		"\u05C1\u05BF\x03\x02\x02\x02\u05C2\u05C3\x07\x04\x02\x02\u05C3G\x03\x02" +
		"\x02\x02\u05C4\u05C5\x07\xDB\x02\x02\u05C5\u05C6\x07\x17\x02\x02\u05C6" +
		"\u05CB\x05J&\x02\u05C7\u05C8\x07\xDB\x02\x02\u05C8\u05C9\x07\x1F\x02\x02" +
		"\u05C9\u05CB\x05L\'\x02\u05CA\u05C4\x03\x02\x02\x02\u05CA\u05C7\x03\x02" +
		"\x02\x02\u05CBI\x03\x02\x02\x02\u05CC\u05CD\x07v\x02\x02\u05CD\u05CE\x07" +
		"\u0119\x02\x02\u05CE\u05CF\x07\xA3\x02\x02\u05CF\u05D2\x07\u0119\x02\x02" +
		"\u05D0\u05D2\x05\u0106\x84\x02\u05D1\u05CC\x03\x02\x02\x02\u05D1\u05D0" +
		"\x03\x02\x02\x02\u05D2K\x03\x02\x02\x02\u05D3\u05D7\x07\u0119\x02\x02" +
		"\u05D4\u05D5\x07\u0104\x02\x02\u05D5\u05D6\x07\xCF\x02\x02\u05D6\u05D8" +
		"\x05<\x1F\x02\u05D7\u05D4\x03\x02\x02\x02\u05D7\u05D8\x03\x02\x02\x02" +
		"\u05D8M\x03\x02\x02\x02\u05D9\u05DA\x05\u0106\x84\x02\u05DA\u05DB\x07" +
		"\u0119\x02\x02\u05DBO\x03\x02\x02\x02\u05DC\u05DD\x05&\x14\x02\u05DD\u05DE" +
		"\x05V,\x02\u05DE\u05DF\x05R*\x02\u05DF\u0610\x03\x02\x02\x02\u05E0\u05E2" +
		"\x05|?\x02\u05E1\u05E3\x05T+\x02\u05E2\u05E1\x03\x02\x02\x02\u05E3\u05E4" +
		"\x03\x02\x02\x02\u05E4\u05E2\x03\x02\x02\x02\u05E4\u05E5\x03\x02\x02\x02" +
		"\u05E5\u0610\x03\x02\x02\x02\u05E6\u05E7\x07C\x02\x02\u05E7\u05E8\x07" +
		"e\x02\x02\u05E8\u05E9\x05\xB2Z\x02\u05E9\u05EB\x05\xACW\x02\u05EA\u05EC" +
		"\x05t;\x02\u05EB\u05EA\x03\x02\x02\x02\u05EB\u05EC\x03\x02\x02\x02\u05EC" +
		"\u0610\x03\x02\x02\x02\u05ED\u05EE\x07\xFA\x02\x02\u05EE\u05EF\x05\xB2" +
		"Z\x02\u05EF\u05F0\x05\xACW\x02\u05F0\u05F2\x05f4\x02\u05F1\u05F3\x05t" +
		";\x02\u05F2\u05F1\x03\x02\x02\x02\u05F2\u05F3\x03\x02\x02\x02\u05F3\u0610" +
		"\x03\x02\x02\x02\u05F4\u05F5\x07\x91\x02\x02\u05F5\u05F6\x07z\x02\x02" +
		"\u05F6\u05F7\x05\xB2Z\x02\u05F7\u05F8\x05\xACW\x02\u05F8\u05FE\x07\xFD" +
		"\x02\x02\u05F9\u05FF\x05\xB2Z\x02\u05FA\u05FB\x07\x03\x02\x02\u05FB\u05FC" +
		"\x05$\x13\x02\u05FC\u05FD\x07\x04\x02\x02\u05FD\u05FF\x03\x02\x02\x02" +
		"\u05FE\u05F9\x03\x02\x02\x02\u05FE\u05FA\x03\x02\x02\x02\u05FF\u0600\x03" +
		"\x02\x02\x02\u0600\u0601\x05\xACW\x02\u0601\u0602\x07\x9B\x02\x02\u0602" +
		"\u0606\x05\xC2b\x02\u0603\u0605\x05h5\x02\u0604\u0603\x03\x02\x02\x02" +
		"\u0605\u0608\x03\x02\x02\x02\u0606\u0604\x03\x02\x02\x02\u0606\u0607\x03" +
		"\x02\x02\x02\u0607\u060C\x03\x02\x02\x02\u0608\u0606\x03\x02\x02\x02\u0609" +
		"\u060B\x05j6\x02\u060A\u0609\x03\x02\x02\x02\u060B\u060E\x03\x02\x02\x02" +
		"\u060C\u060A\x03\x02\x02\x02\u060C\u060D\x03\x02\x02\x02\u060D\u0610\x03" +
		"\x02\x02\x02\u060E\u060C\x03\x02\x02\x02\u060F\u05DC\x03\x02\x02\x02\u060F" +
		"\u05E0\x03\x02\x02\x02\u060F\u05E6\x03\x02\x02\x02\u060F\u05ED\x03\x02" +
		"\x02\x02\u060F\u05F4\x03\x02\x02\x02\u0610Q\x03\x02\x02\x02\u0611\u0612" +
		"\x07\xA0\x02\x02\u0612\u0613\x07\x1F\x02\x02\u0613\u0618\x05Z.\x02\u0614" +
		"\u0615\x07\x05\x02\x02\u0615\u0617\x05Z.\x02\u0616\u0614\x03\x02\x02\x02" +
		"\u0617\u061A\x03\x02\x02\x02\u0618\u0616\x03\x02\x02\x02\u0618\u0619\x03" +
		"\x02\x02\x02\u0619\u061C\x03\x02\x02\x02\u061A\u0618\x03\x02\x02\x02\u061B" +
		"\u0611\x03\x02\x02\x02\u061B\u061C\x03\x02\x02\x02\u061C\u0627\x03\x02" +
		"\x02\x02\u061D\u061E\x07\'\x02\x02\u061E\u061F\x07\x1F\x02\x02\u061F\u0624" +
		"\x05\xC0a\x02\u0620\u0621\x07\x05\x02\x02\u0621\u0623\x05\xC0a\x02\u0622" +
		"\u0620\x03\x02\x02\x02\u0623\u0626\x03\x02\x02\x02\u0624\u0622\x03\x02" +
		"\x02\x02\u0624\u0625\x03\x02\x02\x02\u0625\u0628\x03\x02\x02\x02\u0626" +
		"\u0624\x03\x02\x02\x02\u0627\u061D\x03\x02\x02\x02\u0627\u0628\x03\x02" +
		"\x02\x02\u0628\u0633\x03\x02\x02\x02\u0629\u062A\x07K\x02\x02\u062A\u062B" +
		"\x07\x1F\x02\x02\u062B\u0630\x05\xC0a\x02\u062C\u062D\x07\x05\x02\x02" +
		"\u062D\u062F\x05\xC0a\x02\u062E\u062C\x03\x02\x02\x02\u062F\u0632\x03" +
		"\x02\x02\x02\u0630\u062E\x03\x02\x02\x02\u0630\u0631\x03\x02\x02\x02\u0631" +
		"\u0634\x03\x02\x02\x02\u0632\u0630\x03\x02\x02\x02\u0633\u0629\x03\x02" +
		"\x02\x02\u0633\u0634\x03\x02\x02\x02\u0634\u063F\x03\x02\x02\x02\u0635" +
		"\u0636\x07\xD7\x02\x02\u0636\u0637\x07\x1F\x02\x02\u0637\u063C\x05Z.\x02" +
		"\u0638\u0639\x07\x05\x02\x02\u0639\u063B\x05Z.\x02\u063A\u0638\x03\x02" +
		"\x02\x02\u063B\u063E\x03\x02\x02\x02\u063C\u063A\x03\x02\x02\x02\u063C" +
		"\u063D\x03\x02\x02\x02\u063D\u0640\x03\x02\x02\x02\u063E\u063C\x03\x02" +
		"\x02\x02\u063F\u0635\x03\x02\x02\x02\u063F\u0640\x03\x02\x02\x02\u0640" +
		"\u0642\x03\x02\x02\x02\u0641\u0643\x05\xF2z\x02\u0642\u0641\x03\x02\x02" +
		"\x02\u0642\u0643\x03\x02\x02\x02\u0643\u0649\x03\x02\x02\x02\u0644\u0647" +
		"\x07\x85\x02\x02\u0645\u0648\x07\x0F\x02\x02\u0646\u0648\x05\xC0a\x02" +
		"\u0647\u0645\x03\x02\x02\x02\u0647\u0646\x03\x02\x02\x02\u0648\u064A\x03" +
		"\x02\x02\x02\u0649\u0644\x03\x02\x02\x02\u0649\u064A\x03\x02\x02\x02\u064A" +
		"S\x03\x02\x02\x02\u064B\u064C\x05&\x14\x02\u064C\u064D\x05^0\x02\u064D" +
		"U\x03\x02\x02\x02\u064E\u064F\b,\x01\x02\u064F\u0650\x05X-\x02\u0650\u0668" +
		"\x03\x02\x02\x02\u0651\u0652\f\x05\x02\x02\u0652\u0653\x06,\x03\x02\u0653" +
		"\u0655\t\x10\x02\x02\u0654\u0656\x05\x8CG\x02\u0655\u0654\x03\x02\x02" +
		"\x02\u0655\u0656\x03\x02\x02\x02\u0656\u0657\x03\x02\x02\x02\u0657\u0667" +
		"\x05V,\x06\u0658\u0659\f\x04\x02\x02\u0659\u065A\x06,\x05\x02\u065A\u065C" +
		"\x07x\x02\x02\u065B\u065D\x05\x8CG\x02\u065C\u065B\x03\x02\x02\x02\u065C" +
		"\u065D\x03\x02\x02\x02\u065D\u065E\x03\x02\x02\x02\u065E\u0667\x05V,\x05" +
		"\u065F\u0660\f\x03\x02\x02\u0660\u0661\x06,\x07\x02\u0661\u0663\t\x11" +
		"\x02\x02\u0662\u0664\x05\x8CG\x02\u0663\u0662\x03\x02\x02\x02\u0663\u0664" +
		"\x03\x02\x02\x02\u0664\u0665\x03\x02\x02\x02\u0665\u0667\x05V,\x04\u0666" +
		"\u0651\x03\x02\x02\x02\u0666\u0658\x03\x02\x02\x02\u0666\u065F\x03\x02" +
		"\x02\x02\u0667\u066A\x03\x02\x02\x02\u0668\u0666\x03\x02\x02\x02\u0668" +
		"\u0669\x03\x02\x02\x02\u0669W\x03\x02\x02\x02\u066A\u0668\x03\x02\x02" +
		"\x02\u066B\u0675\x05`1\x02\u066C\u0675\x05\\/\x02\u066D\u066E\x07\xE0" +
		"\x02\x02\u066E\u0675\x05\xB2Z\x02\u066F\u0675\x05\xA8U\x02\u0670\u0671" +
		"\x07\x03\x02\x02\u0671\u0672\x05$\x13\x02\u0672\u0673\x07\x04\x02\x02" +
		"\u0673\u0675\x03\x02\x02\x02\u0674\u066B\x03\x02\x02\x02\u0674\u066C\x03" +
		"\x02\x02\x02\u0674\u066D\x03\x02\x02\x02\u0674\u066F\x03\x02\x02\x02\u0674" +
		"\u0670\x03\x02\x02\x02\u0675Y\x03\x02\x02\x02\u0676\u0678\x05\xC0a\x02" +
		"\u0677\u0679\t\x12\x02\x02\u0678\u0677\x03\x02\x02\x02\u0678\u0679\x03" +
		"\x02\x02\x02\u0679\u067C\x03\x02\x02\x02\u067A\u067B\x07\x99\x02\x02\u067B" +
		"\u067D\t\x13\x02\x02\u067C\u067A\x03\x02\x02\x02\u067C\u067D\x03\x02\x02" +
		"\x02\u067D[\x03\x02\x02\x02\u067E\u0680\x05|?\x02\u067F\u0681\x05^0\x02" +
		"\u0680\u067F\x03\x02\x02\x02\u0681\u0682\x03\x02\x02\x02\u0682\u0680\x03" +
		"\x02\x02\x02\u0682\u0683\x03\x02\x02\x02\u0683]\x03\x02\x02\x02\u0684" +
		"\u0686\x05b2\x02\u0685\u0687\x05t;\x02\u0686\u0685\x03\x02\x02\x02\u0686" +
		"\u0687\x03\x02\x02\x02\u0687\u0688\x03\x02\x02\x02\u0688\u0689\x05R*\x02" +
		"\u0689\u06A0\x03\x02\x02\x02\u068A\u068E\x05d3\x02\u068B\u068D\x05\x8A" +
		"F\x02\u068C\u068B\x03\x02\x02\x02\u068D\u0690\x03\x02\x02\x02\u068E\u068C" +
		"\x03\x02\x02\x02\u068E\u068F\x03\x02\x02\x02\u068F\u0692\x03\x02\x02\x02" +
		"\u0690\u068E\x03\x02\x02\x02\u0691\u0693\x05t;\x02\u0692\u0691\x03\x02" +
		"\x02\x02\u0692\u0693\x03\x02\x02\x02\u0693\u0695\x03\x02\x02\x02\u0694" +
		"\u0696\x05\x80A\x02\u0695\u0694\x03\x02\x02\x02\u0695\u0696\x03\x02\x02" +
		"\x02\u0696\u0698\x03\x02\x02\x02\u0697\u0699\x05v<\x02\u0698\u0697\x03" +
		"\x02\x02\x02\u0698\u0699\x03\x02\x02\x02\u0699\u069B\x03\x02\x02\x02\u069A" +
		"\u069C\x05\xF2z\x02\u069B\u069A\x03\x02\x02\x02\u069B\u069C\x03\x02\x02" +
		"\x02\u069C\u069D\x03\x02\x02\x02\u069D\u069E\x05R*\x02\u069E\u06A0\x03" +
		"\x02\x02\x02\u069F\u0684\x03\x02\x02\x02\u069F\u068A\x03\x02\x02\x02\u06A0" +
		"_\x03\x02\x02\x02\u06A1\u06A3\x05b2\x02\u06A2\u06A4\x05|?\x02\u06A3\u06A2" +
		"\x03\x02\x02\x02\u06A3\u06A4\x03\x02\x02\x02\u06A4\u06A6\x03\x02\x02\x02" +
		"\u06A5\u06A7\x05t;\x02\u06A6\u06A5\x03\x02\x02\x02\u06A6\u06A7\x03\x02" +
		"\x02\x02\u06A7\u06BF\x03\x02\x02\x02\u06A8\u06AA\x05d3\x02\u06A9\u06AB" +
		"\x05|?\x02\u06AA\u06A9\x03\x02\x02\x02\u06AA\u06AB\x03\x02\x02\x02\u06AB" +
		"\u06AF\x03\x02\x02\x02\u06AC\u06AE\x05\x8AF\x02\u06AD\u06AC\x03\x02\x02" +
		"\x02\u06AE\u06B1\x03\x02\x02\x02\u06AF\u06AD\x03\x02\x02\x02\u06AF\u06B0" +
		"\x03\x02\x02\x02\u06B0\u06B3\x03\x02\x02\x02\u06B1\u06AF\x03\x02\x02\x02" +
		"\u06B2\u06B4\x05t;\x02\u06B3\u06B2\x03\x02\x02\x02\u06B3\u06B4\x03\x02" +
		"\x02\x02\u06B4\u06B6\x03\x02\x02\x02\u06B5\u06B7\x05\x80A\x02\u06B6\u06B5" +
		"\x03\x02\x02\x02\u06B6\u06B7\x03\x02\x02\x02\u06B7\u06B9\x03\x02\x02\x02" +
		"\u06B8\u06BA\x05v<\x02\u06B9\u06B8\x03\x02\x02\x02\u06B9\u06BA\x03\x02" +
		"\x02\x02\u06BA\u06BC\x03\x02\x02\x02\u06BB\u06BD\x05\xF2z\x02\u06BC\u06BB" +
		"\x03\x02\x02\x02\u06BC\u06BD\x03\x02\x02\x02\u06BD\u06BF\x03\x02\x02\x02" +
		"\u06BE\u06A1\x03\x02\x02\x02\u06BE\u06A8\x03\x02\x02\x02\u06BFa\x03\x02" +
		"\x02\x02\u06C0\u06C1\x07\xCB\x02\x02\u06C1\u06C2\x07\xED\x02\x02\u06C2" +
		"\u06C3\x07\x03\x02\x02\u06C3\u06C4\x05\xB8]\x02\u06C4\u06C5\x07\x04\x02" +
		"\x02\u06C5\u06CB\x03\x02\x02\x02\u06C6\u06C7\x07\x8F\x02\x02\u06C7\u06CB" +
		"\x05\xB8]\x02\u06C8\u06C9\x07\xB9\x02\x02\u06C9\u06CB\x05\xB8]\x02\u06CA" +
		"\u06C0\x03\x02\x02\x02\u06CA\u06C6\x03\x02\x02\x02\u06CA\u06C8\x03\x02" +
		"\x02\x02\u06CB\u06CD\x03\x02\x02\x02\u06CC\u06CE\x05\xAEX\x02\u06CD\u06CC" +
		"\x03\x02\x02\x02\u06CD\u06CE\x03\x02\x02\x02\u06CE\u06D1\x03\x02\x02\x02" +
		"\u06CF\u06D0\x07\xB7\x02\x02\u06D0\u06D2\x07\u0119\x02\x02\u06D1\u06CF" +
		"\x03\x02\x02\x02\u06D1\u06D2\x03\x02\x02\x02\u06D2\u06D3\x03\x02\x02\x02" +
		"\u06D3\u06D4\x07\xFD\x02\x02\u06D4\u06E1\x07\u0119\x02\x02\u06D5\u06DF" +
		"\x07\x17\x02\x02\u06D6\u06E0\x05\x9CO\x02\u06D7\u06E0\x05\xE8u\x02\u06D8" +
		"\u06DB\x07\x03\x02\x02\u06D9\u06DC\x05\x9CO\x02\u06DA\u06DC\x05\xE8u\x02" +
		"\u06DB\u06D9\x03\x02\x02\x02\u06DB\u06DA\x03\x02\x02\x02\u06DC\u06DD\x03" +
		"\x02\x02\x02\u06DD\u06DE\x07\x04\x02\x02\u06DE\u06E0\x03\x02\x02\x02\u06DF" +
		"\u06D6\x03\x02\x02\x02\u06DF\u06D7\x03\x02\x02\x02\u06DF\u06D8\x03\x02" +
		"\x02\x02\u06E0\u06E2\x03\x02\x02\x02\u06E1\u06D5\x03\x02\x02\x02\u06E1" +
		"\u06E2\x03\x02\x02\x02\u06E2\u06E4\x03\x02\x02\x02\u06E3\u06E5\x05\xAE" +
		"X\x02\u06E4\u06E3\x03\x02\x02\x02\u06E4\u06E5\x03\x02\x02\x02\u06E5\u06E8" +
		"\x03\x02\x02\x02\u06E6\u06E7\x07\xB6\x02\x02\u06E7\u06E9\x07\u0119\x02" +
		"\x02\u06E8\u06E6\x03\x02\x02\x02\u06E8\u06E9\x03\x02\x02\x02\u06E9c\x03" +
		"\x02\x02\x02\u06EA\u06EE\x07\xCB\x02\x02\u06EB\u06ED\x05x=\x02\u06EC\u06EB" +
		"\x03\x02\x02\x02\u06ED\u06F0\x03\x02\x02\x02\u06EE\u06EC\x03\x02\x02\x02" +
		"\u06EE\u06EF\x03\x02\x02\x02\u06EF\u06F2\x03\x02\x02\x02\u06F0\u06EE\x03" +
		"\x02\x02\x02\u06F1\u06F3\x05\x8CG\x02\u06F2\u06F1\x03\x02\x02\x02\u06F2" +
		"\u06F3\x03\x02\x02\x02\u06F3\u06F4\x03\x02\x02\x02\u06F4\u06F5\x05\xB8" +
		"]\x02\u06F5e\x03\x02\x02\x02\u06F6\u06F7\x07\xD1\x02\x02\u06F7\u06F8\x05" +
		"p9\x02\u06F8g\x03\x02\x02\x02\u06F9\u06FA\x07\u0101\x02\x02\u06FA\u06FD" +
		"\x07\x90\x02\x02\u06FB\u06FC\x07\x12\x02\x02\u06FC\u06FE\x05\xC2b\x02" +
		"\u06FD\u06FB\x03\x02\x02\x02\u06FD\u06FE\x03\x02\x02\x02\u06FE\u06FF\x03" +
		"\x02\x02\x02\u06FF\u0700\x07\xE6\x02\x02\u0700\u0701\x05l7\x02\u0701i" +
		"\x03\x02\x02\x02\u0702\u0703\x07\u0101\x02\x02\u0703\u0704\x07\x97\x02" +
		"\x02\u0704\u0707\x07\x90\x02\x02\u0705\u0706\x07\x12\x02\x02\u0706\u0708" +
		"\x05\xC2b\x02\u0707\u0705\x03\x02\x02\x02\u0707\u0708\x03\x02\x02\x02" +
		"\u0708\u0709\x03\x02\x02\x02\u0709\u070A\x07\xE6\x02\x02\u070A\u070B\x05" +
		"n8\x02\u070Bk\x03\x02\x02\x02\u070C\u0714\x07C\x02\x02\u070D\u070E\x07" +
		"\xFA\x02\x02\u070E\u070F\x07\xD1\x02\x02\u070F\u0714\x07\u0110\x02\x02" +
		"\u0710\u0711\x07\xFA\x02\x02\u0711\u0712\x07\xD1\x02\x02\u0712\u0714\x05" +
		"p9\x02\u0713\u070C\x03\x02\x02\x02\u0713\u070D\x03\x02\x02\x02\u0713\u0710" +
		"\x03\x02\x02\x02\u0714m\x03\x02\x02\x02\u0715\u0716\x07w\x02\x02\u0716" +
		"\u0728\x07\u0110\x02\x02\u0717\u0718\x07w\x02\x02\u0718\u0719\x07\x03" +
		"\x02\x02\u0719\u071A\x05\xB0Y\x02\u071A\u071B\x07\x04\x02\x02\u071B\u071C" +
		"\x07\xFE\x02\x02\u071C\u071D\x07\x03\x02\x02\u071D\u0722\x05\xC0a\x02" +
		"\u071E\u071F\x07\x05\x02\x02\u071F\u0721\x05\xC0a\x02\u0720\u071E\x03" +
		"\x02\x02\x02\u0721\u0724\x03\x02\x02\x02\u0722\u0720\x03\x02\x02\x02\u0722" +
		"\u0723\x03\x02\x02\x02\u0723\u0725\x03\x02\x02\x02\u0724\u0722\x03\x02" +
		"\x02\x02\u0725\u0726\x07\x04\x02\x02\u0726\u0728\x03\x02\x02\x02\u0727" +
		"\u0715\x03\x02\x02\x02\u0727\u0717\x03\x02\x02\x02\u0728o\x03\x02\x02" +
		"\x02\u0729\u072E\x05r:\x02\u072A\u072B\x07\x05\x02\x02\u072B\u072D\x05" +
		"r:\x02\u072C\u072A\x03\x02\x02\x02\u072D\u0730\x03\x02\x02\x02\u072E\u072C" +
		"\x03\x02\x02\x02\u072E\u072F\x03\x02\x02\x02\u072Fq\x03\x02\x02\x02\u0730" +
		"\u072E\x03\x02\x02\x02\u0731\u0732\x05\xB2Z\x02\u0732\u0733\x07\u0106" +
		"\x02\x02\u0733\u0734\x05\xC0a\x02\u0734s\x03\x02\x02\x02\u0735\u0736\x07" +
		"\u0102\x02\x02\u0736\u0737\x05\xC2b\x02\u0737u\x03\x02\x02\x02\u0738\u0739" +
		"\x07m\x02\x02\u0739\u073A\x05\xC2b\x02\u073Aw\x03\x02\x02\x02\u073B\u073C" +
		"\x07\x07\x02\x02\u073C\u0743\x05z>\x02\u073D\u073F\x07\x05\x02\x02\u073E" +
		"\u073D\x03\x02\x02\x02\u073E\u073F\x03\x02\x02\x02\u073F\u0740\x03\x02" +
		"\x02\x02\u0740\u0742\x05z>\x02\u0741\u073E\x03\x02\x02\x02\u0742\u0745" +
		"\x03\x02\x02\x02\u0743\u0741\x03\x02\x02\x02\u0743\u0744\x03\x02\x02\x02" +
		"\u0744\u0746\x03\x02\x02\x02\u0745\u0743\x03\x02\x02\x02\u0746\u0747\x07" +
		"\b\x02\x02\u0747y\x03\x02\x02\x02\u0748\u0756\x05\u0106\x84\x02\u0749" +
		"\u074A\x05\u0106\x84\x02\u074A\u074B\x07\x03\x02\x02\u074B\u0750\x05\xC8" +
		"e\x02\u074C\u074D\x07\x05\x02\x02\u074D\u074F\x05\xC8e\x02\u074E\u074C" +
		"\x03\x02\x02\x02\u074F\u0752\x03\x02\x02\x02\u0750\u074E\x03\x02\x02\x02" +
		"\u0750\u0751\x03\x02\x02\x02\u0751\u0753\x03\x02\x02\x02\u0752\u0750\x03" +
		"\x02\x02\x02\u0753\u0754\x07\x04\x02\x02\u0754\u0756\x03\x02\x02\x02\u0755" +
		"\u0748\x03\x02\x02\x02\u0755\u0749\x03\x02\x02\x02\u0756{\x03\x02\x02" +
		"\x02\u0757\u0758\x07e\x02\x02\u0758\u0759\x05\x8EH\x02\u0759\u0760\x05" +
		"~@\x02\u075A\u075B\x07\x05\x02\x02\u075B\u075C\x05~@\x02\u075C\u075D\x05" +
		"\x8EH\x02\u075D\u075F\x03\x02\x02\x02\u075E\u075A\x03\x02\x02\x02\u075F" +
		"\u0762\x03\x02\x02\x02\u0760\u075E\x03\x02";
	private static readonly _serializedATNSegment4: string =
		"\x02\x02\u0760\u0761\x03\x02\x02\x02\u0761\u0766\x03\x02\x02\x02\u0762" +
		"\u0760\x03\x02\x02\x02\u0763\u0765\x05\x8AF\x02\u0764\u0763\x03\x02\x02" +
		"\x02\u0765\u0768\x03\x02\x02\x02\u0766\u0764\x03\x02\x02\x02\u0766\u0767" +
		"\x03\x02\x02\x02\u0767\u076A\x03\x02\x02\x02\u0768\u0766\x03\x02\x02\x02" +
		"\u0769\u076B\x05\x84C\x02\u076A\u0769\x03\x02\x02\x02\u076A\u076B\x03" +
		"\x02\x02\x02\u076B}\x03\x02\x02\x02\u076C\u076D\x03\x02\x02\x02\u076D" +
		"\x7F\x03\x02\x02\x02\u076E\u076F\x07k\x02\x02\u076F\u0770\x07\x1F\x02" +
		"\x02\u0770\u0775\x05\xC0a\x02\u0771\u0772\x07\x05\x02\x02\u0772\u0774" +
		"\x05\xC0a\x02\u0773\u0771\x03\x02\x02\x02\u0774\u0777\x03\x02\x02\x02" +
		"\u0775\u0773\x03\x02\x02\x02\u0775\u0776\x03\x02\x02\x02\u0776\u0789\x03" +
		"\x02\x02\x02\u0777\u0775\x03\x02\x02\x02\u0778\u0779\x07\u0104\x02\x02" +
		"\u0779\u078A\x07\xC7\x02\x02\u077A\u077B\x07\u0104\x02\x02\u077B\u078A" +
		"\x078\x02\x02\u077C\u077D\x07l\x02\x02\u077D\u077E\x07\xD3\x02\x02\u077E" +
		"\u077F\x07\x03\x02\x02\u077F\u0784\x05\x82B\x02\u0780\u0781\x07\x05\x02" +
		"\x02\u0781\u0783\x05\x82B\x02\u0782\u0780\x03\x02\x02\x02\u0783\u0786" +
		"\x03\x02\x02\x02\u0784\u0782\x03\x02\x02\x02\u0784\u0785\x03\x02\x02\x02" +
		"\u0785\u0787\x03\x02\x02\x02\u0786\u0784\x03\x02\x02\x02\u0787\u0788\x07" +
		"\x04\x02\x02\u0788\u078A\x03\x02\x02\x02\u0789\u0778\x03\x02\x02\x02\u0789" +
		"\u077A\x03\x02\x02\x02\u0789\u077C\x03\x02\x02\x02\u0789\u078A\x03\x02" +
		"\x02\x02\u078A\u079B\x03\x02\x02\x02\u078B\u078C\x07k\x02\x02\u078C\u078D" +
		"\x07\x1F\x02\x02\u078D\u078E\x07l\x02\x02\u078E\u078F\x07\xD3\x02\x02" +
		"\u078F\u0790\x07\x03\x02\x02\u0790\u0795\x05\x82B\x02\u0791\u0792\x07" +
		"\x05\x02\x02\u0792\u0794\x05\x82B\x02\u0793\u0791\x03\x02\x02\x02\u0794" +
		"\u0797\x03\x02\x02\x02\u0795\u0793\x03\x02\x02\x02\u0795\u0796\x03\x02" +
		"\x02\x02\u0796\u0798\x03\x02\x02\x02\u0797\u0795\x03\x02\x02\x02\u0798" +
		"\u0799\x07\x04\x02\x02\u0799\u079B\x03\x02\x02\x02\u079A\u076E\x03\x02" +
		"\x02\x02\u079A\u078B\x03\x02\x02\x02\u079B\x81\x03\x02\x02\x02\u079C\u07A5" +
		"\x07\x03\x02\x02\u079D\u07A2\x05\xC0a\x02\u079E\u079F\x07\x05\x02\x02" +
		"\u079F\u07A1\x05\xC0a\x02\u07A0\u079E\x03\x02\x02\x02\u07A1\u07A4\x03" +
		"\x02\x02\x02\u07A2\u07A0\x03\x02\x02\x02\u07A2\u07A3\x03\x02\x02\x02\u07A3" +
		"\u07A6\x03\x02\x02\x02\u07A4\u07A2\x03\x02\x02\x02\u07A5\u079D\x03\x02" +
		"\x02\x02\u07A5\u07A6\x03\x02\x02\x02\u07A6\u07A7\x03\x02\x02\x02\u07A7" +
		"\u07AA\x07\x04\x02\x02\u07A8\u07AA\x05\xC0a\x02\u07A9\u079C\x03\x02\x02" +
		"\x02\u07A9\u07A8\x03\x02\x02\x02\u07AA\x83\x03\x02\x02\x02\u07AB\u07AC" +
		"\x07\xAC\x02\x02\u07AC\u07AD\x07\x03\x02\x02\u07AD\u07AE\x05\xB8]\x02" +
		"\u07AE\u07AF\x07a\x02\x02\u07AF\u07B0\x05\x86D\x02\u07B0\u07B1\x07q\x02" +
		"\x02\u07B1\u07B2\x07\x03\x02\x02\u07B2\u07B7\x05\x88E\x02\u07B3\u07B4" +
		"\x07\x05\x02\x02\u07B4\u07B6\x05\x88E\x02\u07B5\u07B3\x03\x02\x02\x02" +
		"\u07B6\u07B9\x03\x02\x02\x02\u07B7\u07B5\x03\x02\x02\x02\u07B7\u07B8\x03" +
		"\x02\x02\x02\u07B8\u07BA\x03\x02\x02\x02\u07B9\u07B7\x03\x02\x02\x02\u07BA" +
		"\u07BB\x07\x04\x02\x02\u07BB\u07BC\x07\x04\x02\x02\u07BC\x85\x03\x02\x02" +
		"\x02\u07BD\u07CA\x05\u0106\x84\x02\u07BE\u07BF\x07\x03\x02\x02\u07BF\u07C4" +
		"\x05\u0106\x84\x02\u07C0\u07C1\x07\x05\x02\x02\u07C1\u07C3\x05\u0106\x84" +
		"\x02\u07C2\u07C0\x03\x02\x02\x02\u07C3\u07C6\x03\x02\x02\x02\u07C4\u07C2" +
		"\x03\x02\x02\x02\u07C4\u07C5\x03\x02\x02\x02\u07C5\u07C7\x03\x02\x02\x02" +
		"\u07C6\u07C4\x03\x02\x02\x02\u07C7\u07C8\x07\x04\x02\x02\u07C8\u07CA\x03" +
		"\x02\x02\x02\u07C9\u07BD\x03\x02\x02\x02\u07C9\u07BE\x03\x02\x02\x02\u07CA" +
		"\x87\x03\x02\x02\x02\u07CB\u07D0\x05\xC0a\x02\u07CC\u07CE\x07\x17\x02" +
		"\x02\u07CD\u07CC\x03\x02\x02\x02\u07CD\u07CE\x03\x02\x02\x02\u07CE\u07CF" +
		"\x03\x02\x02\x02\u07CF\u07D1\x05\u0106\x84\x02\u07D0\u07CD\x03\x02\x02" +
		"\x02\u07D0\u07D1\x03\x02\x02\x02\u07D1\x89\x03\x02\x02\x02\u07D2\u07D3" +
		"\x07\x80\x02\x02\u07D3\u07D5\x07\xFF\x02\x02\u07D4\u07D6\x07\xA2\x02\x02" +
		"\u07D5\u07D4\x03\x02\x02\x02\u07D5\u07D6\x03\x02\x02\x02\u07D6\u07D7\x03" +
		"\x02\x02\x02\u07D7\u07D8\x05\u0100\x81\x02\u07D8\u07E1\x07\x03\x02\x02" +
		"\u07D9\u07DE\x05\xC0a\x02\u07DA\u07DB\x07\x05\x02\x02\u07DB\u07DD\x05" +
		"\xC0a\x02\u07DC\u07DA\x03\x02\x02\x02\u07DD\u07E0\x03\x02\x02\x02\u07DE" +
		"\u07DC\x03\x02\x02\x02\u07DE\u07DF\x03\x02\x02\x02\u07DF\u07E2\x03\x02" +
		"\x02\x02\u07E0\u07DE\x03\x02\x02\x02\u07E1\u07D9\x03\x02\x02\x02\u07E1" +
		"\u07E2\x03\x02\x02\x02\u07E2\u07E3\x03\x02\x02\x02\u07E3\u07E4\x07\x04" +
		"\x02\x02\u07E4\u07F0\x05\u0106\x84\x02\u07E5\u07E7\x07\x17\x02\x02\u07E6" +
		"\u07E5\x03\x02\x02\x02\u07E6\u07E7\x03\x02\x02\x02\u07E7\u07E8\x03\x02" +
		"\x02\x02\u07E8\u07ED\x05\u0106\x84\x02\u07E9\u07EA\x07\x05\x02\x02\u07EA" +
		"\u07EC\x05\u0106\x84\x02\u07EB\u07E9\x03\x02\x02\x02\u07EC\u07EF\x03\x02" +
		"\x02\x02\u07ED\u07EB\x03\x02\x02\x02\u07ED\u07EE\x03\x02\x02\x02\u07EE" +
		"\u07F1\x03\x02\x02\x02\u07EF\u07ED\x03\x02\x02\x02\u07F0\u07E6\x03\x02" +
		"\x02\x02\u07F0\u07F1\x03\x02\x02\x02\u07F1\x8B\x03\x02\x02\x02\u07F2\u07F3" +
		"\t\x14\x02\x02\u07F3\x8D\x03\x02\x02\x02\u07F4\u07F8\x05\xA6T\x02\u07F5" +
		"\u07F7\x05\x90I\x02\u07F6\u07F5\x03\x02\x02\x02\u07F7\u07FA\x03\x02\x02" +
		"\x02\u07F8\u07F6\x03\x02\x02\x02\u07F8\u07F9\x03\x02\x02\x02\u07F9\x8F" +
		"\x03\x02\x02\x02\u07FA\u07F8\x03\x02\x02\x02\u07FB\u07FC\x05\x92J\x02" +
		"\u07FC\u07FD\x07}\x02\x02\u07FD\u07FF\x05\xA6T\x02\u07FE\u0800\x05\x94" +
		"K\x02\u07FF\u07FE\x03\x02\x02\x02\u07FF\u0800\x03\x02\x02\x02\u0800\u0807" +
		"\x03\x02\x02\x02\u0801\u0802\x07\x95\x02\x02\u0802\u0803\x05\x92J\x02" +
		"\u0803\u0804\x07}\x02\x02\u0804\u0805\x05\xA6T\x02\u0805\u0807\x03\x02" +
		"\x02\x02\u0806\u07FB\x03\x02\x02\x02\u0806\u0801\x03\x02\x02\x02\u0807" +
		"\x91\x03\x02\x02\x02\u0808\u080A\x07t\x02\x02\u0809\u0808\x03\x02\x02" +
		"\x02\u0809\u080A\x03\x02\x02\x02\u080A\u0821\x03\x02\x02\x02\u080B\u0821" +
		"\x077\x02\x02\u080C\u080E\x07\x83\x02\x02\u080D\u080F\x07\xA2\x02\x02" +
		"\u080E\u080D\x03\x02\x02\x02\u080E\u080F\x03\x02\x02\x02\u080F\u0821\x03" +
		"\x02\x02\x02\u0810\u0812\x07\x83\x02\x02\u0811\u0810\x03\x02\x02\x02\u0811" +
		"\u0812\x03\x02\x02\x02\u0812\u0813\x03\x02\x02\x02\u0813\u0821\x07\xCC" +
		"\x02\x02\u0814\u0816\x07\xC2\x02\x02\u0815\u0817\x07\xA2\x02\x02\u0816" +
		"\u0815\x03\x02\x02\x02\u0816\u0817\x03\x02\x02\x02\u0817\u0821\x03\x02" +
		"\x02\x02\u0818\u081A\x07f\x02\x02\u0819\u081B\x07\xA2\x02\x02\u081A\u0819" +
		"\x03\x02\x02\x02\u081A\u081B\x03\x02\x02\x02\u081B\u0821\x03\x02\x02\x02" +
		"\u081C\u081E\x07\x83\x02\x02\u081D\u081C\x03\x02\x02\x02\u081D\u081E\x03" +
		"\x02\x02\x02\u081E\u081F\x03\x02\x02\x02\u081F\u0821\x07\x13\x02\x02\u0820" +
		"\u0809\x03\x02\x02\x02\u0820\u080B\x03\x02\x02\x02\u0820\u080C\x03\x02" +
		"\x02\x02\u0820\u0811\x03\x02\x02\x02\u0820\u0814\x03\x02\x02\x02\u0820" +
		"\u0818\x03\x02\x02\x02\u0820\u081D\x03\x02\x02\x02\u0821\x93\x03\x02\x02" +
		"\x02\u0822\u0823\x07\x9B\x02\x02\u0823\u0827\x05\xC2b\x02\u0824\u0825" +
		"\x07\xFD\x02\x02\u0825\u0827\x05\x9AN\x02\u0826\u0822\x03\x02\x02\x02" +
		"\u0826\u0824\x03\x02\x02\x02\u0827\x95\x03\x02\x02\x02\u0828\u0829\x07" +
		"\xE2\x02\x02\u0829\u082B\x07\x03\x02\x02\u082A\u082C\x05\x98M\x02\u082B" +
		"\u082A\x03\x02\x02\x02\u082B\u082C\x03\x02\x02\x02\u082C\u082D\x03\x02" +
		"\x02\x02\u082D\u082E\x07\x04\x02\x02\u082E\x97\x03\x02\x02\x02\u082F\u0831" +
		"\x07\u010F\x02\x02\u0830\u082F\x03\x02\x02\x02\u0830\u0831\x03\x02\x02" +
		"\x02\u0831\u0832\x03\x02\x02\x02\u0832\u0833\t\x15\x02\x02\u0833\u0848" +
		"\x07\xAB\x02\x02\u0834\u0835\x05\xC0a\x02\u0835\u0836\x07\xC9\x02\x02" +
		"\u0836\u0848\x03\x02\x02\x02\u0837\u0838\x07\x1D\x02\x02\u0838\u0839\x07" +
		"\u011D\x02\x02\u0839\u083A\x07\xA1\x02\x02\u083A\u083B\x07\x9A\x02\x02" +
		"\u083B\u0844\x07\u011D\x02\x02\u083C\u0842\x07\x9B\x02\x02\u083D\u0843" +
		"\x05\u0106\x84\x02\u083E\u083F\x05\u0100\x81\x02\u083F\u0840\x07\x03\x02" +
		"\x02\u0840\u0841\x07\x04\x02\x02\u0841\u0843\x03\x02\x02\x02\u0842\u083D" +
		"\x03\x02\x02\x02\u0842\u083E\x03\x02\x02\x02\u0843\u0845\x03\x02\x02\x02" +
		"\u0844\u083C\x03\x02\x02\x02\u0844\u0845\x03\x02\x02\x02\u0845\u0848\x03" +
		"\x02\x02\x02\u0846\u0848\x05\xC0a\x02\u0847\u0830\x03\x02\x02\x02\u0847" +
		"\u0834\x03\x02\x02\x02\u0847\u0837\x03\x02\x02\x02\u0847\u0846\x03\x02" +
		"\x02\x02\u0848\x99\x03\x02\x02\x02\u0849\u084A\x07\x03\x02\x02\u084A\u084B" +
		"\x05\x9CO\x02\u084B\u084C\x07\x04\x02\x02\u084C\x9B\x03\x02\x02\x02\u084D" +
		"\u0852\x05\u0102\x82\x02\u084E\u084F\x07\x05\x02\x02\u084F\u0851\x05\u0102" +
		"\x82\x02\u0850\u084E\x03\x02\x02\x02\u0851\u0854\x03\x02\x02\x02\u0852" +
		"\u0850\x03\x02\x02\x02\u0852\u0853\x03\x02\x02\x02\u0853\x9D\x03\x02\x02" +
		"\x02\u0854\u0852\x03\x02\x02\x02\u0855\u0856\x07\x03\x02\x02\u0856\u085B" +
		"\x05\xA0Q\x02\u0857\u0858\x07\x05\x02\x02\u0858\u085A\x05\xA0Q\x02\u0859" +
		"\u0857\x03\x02\x02\x02\u085A\u085D\x03\x02\x02\x02\u085B\u0859\x03\x02" +
		"\x02\x02\u085B\u085C\x03\x02\x02\x02\u085C\u085E\x03\x02\x02\x02\u085D" +
		"\u085B\x03\x02\x02\x02\u085E\u085F\x07\x04\x02\x02\u085F\x9F\x03\x02\x02" +
		"\x02\u0860\u0862\x05\u0102\x82\x02\u0861\u0863\t\x12\x02\x02\u0862\u0861" +
		"\x03\x02\x02\x02\u0862\u0863\x03\x02\x02\x02\u0863\xA1\x03\x02\x02\x02" +
		"\u0864\u0865\x07\x03\x02\x02\u0865\u086A\x05\xA4S\x02\u0866\u0867\x07" +
		"\x05\x02\x02\u0867\u0869\x05\xA4S\x02\u0868\u0866\x03\x02\x02\x02\u0869" +
		"\u086C\x03\x02\x02\x02\u086A\u0868\x03\x02\x02\x02\u086A\u086B\x03\x02" +
		"\x02\x02\u086B\u086D\x03\x02\x02\x02\u086C\u086A\x03\x02\x02\x02\u086D" +
		"\u086E\x07\x04\x02\x02\u086E\xA3\x03\x02\x02\x02\u086F\u0871\x05\u0106" +
		"\x84\x02\u0870\u0872\x05\"\x12\x02\u0871\u0870\x03\x02\x02\x02\u0871\u0872" +
		"\x03\x02\x02\x02\u0872\xA5\x03\x02\x02\x02\u0873\u0875\x05\xB2Z\x02\u0874" +
		"\u0876\x05\x96L\x02\u0875\u0874\x03\x02\x02\x02\u0875\u0876\x03\x02\x02" +
		"\x02\u0876\u0877\x03\x02\x02\x02\u0877\u0878\x05\xACW\x02\u0878\u088C" +
		"\x03\x02\x02\x02\u0879\u087A\x07\x03\x02\x02\u087A\u087B\x05$\x13\x02" +
		"\u087B\u087D\x07\x04\x02\x02\u087C\u087E\x05\x96L\x02\u087D\u087C\x03" +
		"\x02\x02\x02\u087D\u087E\x03\x02\x02\x02\u087E\u087F\x03\x02\x02\x02\u087F" +
		"\u0880\x05\xACW\x02\u0880\u088C\x03\x02\x02\x02\u0881\u0882\x07\x03\x02" +
		"\x02\u0882\u0883\x05\x8EH\x02\u0883\u0885\x07\x04\x02\x02\u0884\u0886" +
		"\x05\x96L\x02\u0885\u0884\x03\x02\x02\x02\u0885\u0886\x03\x02\x02\x02" +
		"\u0886\u0887\x03\x02\x02\x02\u0887\u0888\x05\xACW\x02\u0888\u088C\x03" +
		"\x02\x02\x02\u0889\u088C\x05\xA8U\x02\u088A\u088C\x05\xAAV\x02\u088B\u0873" +
		"\x03\x02\x02\x02\u088B\u0879\x03\x02\x02\x02\u088B\u0881\x03\x02\x02\x02" +
		"\u088B\u0889\x03\x02\x02\x02\u088B\u088A\x03\x02\x02\x02\u088C\xA7\x03" +
		"\x02\x02\x02\u088D\u088E\x07\xFE\x02\x02\u088E\u0893\x05\xC0a\x02\u088F" +
		"\u0890\x07\x05\x02\x02\u0890\u0892\x05\xC0a\x02\u0891\u088F\x03\x02\x02" +
		"\x02\u0892\u0895\x03\x02\x02\x02\u0893\u0891\x03\x02\x02\x02\u0893\u0894" +
		"\x03\x02\x02\x02\u0894\u0896\x03\x02\x02\x02\u0895\u0893\x03\x02\x02\x02" +
		"\u0896\u0897\x05\xACW\x02\u0897\xA9\x03\x02\x02\x02\u0898\u0899\x05\u0102" +
		"\x82\x02\u0899\u08A2\x07\x03\x02\x02\u089A\u089F\x05\xC0a\x02\u089B\u089C" +
		"\x07\x05\x02\x02\u089C\u089E\x05\xC0a\x02\u089D\u089B\x03\x02\x02\x02" +
		"\u089E\u08A1\x03\x02\x02\x02\u089F\u089D\x03\x02\x02\x02\u089F\u08A0\x03" +
		"\x02\x02\x02\u08A0\u08A3\x03\x02\x02\x02\u08A1\u089F\x03\x02\x02\x02\u08A2" +
		"\u089A\x03\x02\x02\x02\u08A2\u08A3\x03\x02\x02\x02\u08A3\u08A4\x03\x02" +
		"\x02\x02\u08A4\u08A5\x07\x04\x02\x02\u08A5\u08A6\x05\xACW\x02\u08A6\xAB" +
		"\x03\x02\x02\x02\u08A7\u08A9\x07\x17\x02\x02\u08A8\u08A7\x03\x02\x02\x02" +
		"\u08A8\u08A9\x03\x02\x02\x02\u08A9\u08AA\x03\x02\x02\x02\u08AA\u08AC\x05" +
		"\u0108\x85\x02\u08AB\u08AD\x05\x9AN\x02\u08AC\u08AB\x03\x02\x02\x02\u08AC" +
		"\u08AD\x03\x02\x02\x02\u08AD\u08AF\x03\x02\x02\x02\u08AE\u08A8\x03\x02" +
		"\x02\x02\u08AE\u08AF\x03\x02\x02\x02\u08AF\xAD\x03\x02\x02\x02\u08B0\u08B1" +
		"\x07\xC8\x02\x02\u08B1\u08B2\x07c\x02\x02\u08B2\u08B3\x07\xCE\x02\x02" +
		"\u08B3\u08B7\x07\u0119\x02\x02\u08B4\u08B5\x07\u0104\x02\x02\u08B5\u08B6" +
		"\x07\xCF\x02\x02\u08B6\u08B8\x05<\x1F\x02\u08B7\u08B4\x03\x02\x02\x02" +
		"\u08B7\u08B8\x03\x02\x02\x02\u08B8\u08E2\x03\x02\x02\x02\u08B9\u08BA\x07" +
		"\xC8\x02\x02\u08BA\u08BB\x07c\x02\x02\u08BB\u08C5\x07D\x02\x02\u08BC\u08BD" +
		"\x07\\\x02\x02\u08BD\u08BE\x07\xE5\x02\x02\u08BE\u08BF\x07\x1F\x02\x02" +
		"\u08BF\u08C3\x07\u0119\x02\x02\u08C0\u08C1\x07Q\x02\x02\u08C1\u08C2\x07" +
		"\x1F\x02\x02\u08C2\u08C4\x07\u0119\x02\x02\u08C3\u08C0\x03\x02\x02\x02" +
		"\u08C3\u08C4\x03\x02\x02\x02\u08C4\u08C6\x03\x02\x02\x02\u08C5\u08BC\x03" +
		"\x02\x02\x02\u08C5\u08C6\x03\x02\x02\x02\u08C6\u08CC\x03\x02\x02\x02\u08C7" +
		"\u08C8\x07+\x02\x02\u08C8\u08C9\x07|\x02\x02\u08C9\u08CA\x07\xE5\x02\x02" +
		"\u08CA\u08CB\x07\x1F\x02\x02\u08CB\u08CD\x07\u0119\x02\x02\u08CC\u08C7" +
		"\x03\x02\x02\x02\u08CC\u08CD\x03\x02\x02\x02\u08CD\u08D3\x03\x02\x02\x02" +
		"\u08CE\u08CF\x07\x8F\x02\x02\u08CF\u08D0\x07~\x02\x02\u08D0\u08D1\x07" +
		"\xE5\x02\x02\u08D1\u08D2\x07\x1F\x02\x02\u08D2\u08D4\x07\u0119\x02\x02" +
		"\u08D3\u08CE\x03\x02\x02\x02\u08D3\u08D4\x03\x02\x02\x02\u08D4\u08D9\x03" +
		"\x02\x02\x02\u08D5\u08D6\x07\x86\x02\x02\u08D6\u08D7\x07\xE5\x02\x02\u08D7" +
		"\u08D8\x07\x1F\x02\x02\u08D8\u08DA\x07\u0119\x02\x02\u08D9\u08D5\x03\x02" +
		"\x02\x02\u08D9\u08DA\x03\x02\x02\x02\u08DA\u08DF\x03\x02\x02\x02\u08DB" +
		"\u08DC\x07\x98\x02\x02\u08DC\u08DD\x07B\x02\x02\u08DD\u08DE\x07\x17\x02" +
		"\x02\u08DE\u08E0\x07\u0119\x02\x02\u08DF\u08DB\x03\x02\x02\x02\u08DF\u08E0" +
		"\x03\x02\x02\x02\u08E0\u08E2\x03\x02\x02\x02\u08E1\u08B0\x03\x02\x02\x02" +
		"\u08E1\u08B9\x03\x02\x02\x02\u08E2\xAF\x03\x02\x02\x02\u08E3\u08E8\x05" +
		"\xB2Z\x02\u08E4\u08E5\x07\x05\x02\x02\u08E5\u08E7\x05\xB2Z\x02\u08E6\u08E4" +
		"\x03\x02\x02\x02\u08E7\u08EA\x03\x02\x02\x02\u08E8\u08E6\x03\x02\x02\x02" +
		"\u08E8\u08E9\x03\x02\x02\x02\u08E9\xB1\x03\x02\x02\x02\u08EA\u08E8\x03" +
		"\x02\x02\x02\u08EB\u08F0\x05\u0102\x82\x02\u08EC\u08ED\x07\x06\x02\x02" +
		"\u08ED\u08EF\x05\u0102\x82\x02\u08EE\u08EC\x03\x02\x02\x02\u08EF\u08F2" +
		"\x03\x02\x02\x02\u08F0\u08EE\x03\x02\x02\x02\u08F0\u08F1\x03\x02\x02\x02" +
		"\u08F1\xB3\x03\x02\x02\x02\u08F2\u08F0\x03\x02\x02\x02\u08F3\u08F4\x05" +
		"\u0102\x82\x02\u08F4\u08F5\x07\x06\x02\x02\u08F5\u08F7\x03\x02\x02\x02" +
		"\u08F6\u08F3\x03\x02\x02\x02\u08F6\u08F7\x03\x02\x02\x02\u08F7\u08F8\x03" +
		"\x02\x02\x02\u08F8\u08F9\x05\u0102\x82\x02\u08F9\xB5\x03\x02\x02\x02\u08FA" +
		"\u0902\x05\xC0a\x02\u08FB\u08FD\x07\x17\x02\x02\u08FC\u08FB\x03\x02\x02" +
		"\x02\u08FC\u08FD\x03\x02\x02\x02\u08FD\u0900\x03\x02\x02\x02\u08FE\u0901" +
		"\x05\u0102\x82\x02\u08FF\u0901\x05\x9AN\x02\u0900\u08FE\x03\x02\x02\x02" +
		"\u0900\u08FF\x03\x02\x02\x02\u0901\u0903\x03\x02\x02\x02\u0902\u08FC\x03" +
		"\x02\x02\x02\u0902\u0903\x03\x02\x02\x02\u0903\xB7\x03\x02\x02\x02\u0904" +
		"\u0909\x05\xB6\\\x02\u0905\u0906\x07\x05\x02\x02\u0906\u0908\x05\xB6\\" +
		"\x02\u0907\u0905\x03\x02\x02\x02\u0908\u090B\x03\x02\x02\x02\u0909\u0907" +
		"\x03\x02\x02\x02\u0909\u090A\x03\x02\x02\x02\u090A\xB9\x03\x02\x02\x02" +
		"\u090B\u0909\x03\x02\x02\x02\u090C\u090D\x07\x03\x02\x02\u090D\u0912\x05" +
		"\xBC_\x02\u090E\u090F\x07\x05\x02\x02\u090F\u0911\x05\xBC_\x02\u0910\u090E" +
		"\x03\x02\x02\x02\u0911\u0914\x03\x02\x02\x02\u0912\u0910\x03\x02\x02\x02" +
		"\u0912\u0913\x03\x02\x02\x02\u0913\u0915\x03\x02\x02\x02\u0914\u0912\x03" +
		"\x02\x02\x02\u0915\u0916\x07\x04\x02\x02\u0916\xBB\x03\x02\x02\x02\u0917" +
		"\u0925\x05\u0100\x81\x02\u0918\u0919\x05\u0106\x84\x02\u0919\u091A\x07" +
		"\x03\x02\x02\u091A\u091F\x05\xBE`\x02\u091B\u091C\x07\x05\x02\x02\u091C" +
		"\u091E\x05\xBE`\x02\u091D\u091B\x03\x02\x02\x02\u091E\u0921\x03\x02\x02" +
		"\x02\u091F\u091D\x03\x02\x02\x02\u091F\u0920\x03\x02\x02\x02\u0920\u0922" +
		"\x03\x02\x02\x02\u0921\u091F\x03\x02\x02\x02\u0922\u0923\x07\x04\x02\x02" +
		"\u0923\u0925\x03\x02\x02\x02\u0924\u0917\x03\x02\x02\x02\u0924\u0918\x03" +
		"\x02\x02\x02\u0925\xBD\x03\x02\x02\x02\u0926\u0929\x05\u0100\x81\x02\u0927" +
		"\u0929\x05\xCAf\x02\u0928\u0926\x03\x02\x02\x02\u0928\u0927\x03\x02\x02" +
		"\x02\u0929\xBF\x03\x02\x02\x02\u092A\u092B\x05\xC2b\x02\u092B\xC1\x03" +
		"\x02\x02\x02\u092C\u092D\bb\x01\x02\u092D\u092E\x07\x97\x02\x02\u092E" +
		"\u0939\x05\xC2b\x07\u092F\u0930\x07T\x02\x02\u0930\u0931\x07\x03\x02\x02" +
		"\u0931\u0932\x05$\x13\x02\u0932\u0933\x07\x04\x02\x02\u0933\u0939\x03" +
		"\x02\x02\x02\u0934\u0936\x05\xC6d\x02\u0935\u0937\x05\xC4c\x02\u0936\u0935" +
		"\x03\x02\x02\x02\u0936\u0937\x03\x02\x02\x02\u0937\u0939\x03\x02\x02\x02" +
		"\u0938\u092C\x03\x02\x02\x02\u0938\u092F\x03\x02\x02\x02\u0938\u0934\x03" +
		"\x02\x02\x02\u0939\u0942\x03\x02\x02\x02\u093A\u093B\f\x04\x02\x02\u093B" +
		"\u093C\x07\x12\x02\x02\u093C\u0941\x05\xC2b\x05\u093D\u093E\f\x03\x02" +
		"\x02\u093E\u093F\x07\x9F\x02\x02\u093F\u0941\x05\xC2b\x04\u0940\u093A" +
		"\x03\x02\x02\x02\u0940\u093D\x03\x02\x02\x02\u0941\u0944\x03\x02\x02\x02" +
		"\u0942\u0940\x03\x02\x02\x02\u0942\u0943\x03\x02\x02\x02\u0943\xC3\x03" +
		"\x02\x02\x02\u0944\u0942\x03\x02\x02\x02\u0945\u0947\x07\x97\x02\x02\u0946" +
		"\u0945\x03\x02\x02\x02\u0946\u0947\x03\x02\x02\x02\u0947\u0948\x03\x02" +
		"\x02\x02\u0948\u0949\x07\x1B\x02\x02\u0949\u094A\x05\xC6d\x02\u094A\u094B" +
		"\x07\x12\x02\x02\u094B\u094C\x05\xC6d\x02\u094C\u0998\x03\x02\x02\x02" +
		"\u094D\u094F\x07\x97\x02\x02\u094E\u094D\x03\x02\x02\x02\u094E\u094F\x03" +
		"\x02\x02\x02\u094F\u0950\x03\x02\x02\x02\u0950\u0951\x07q\x02\x02\u0951" +
		"\u0952\x07\x03\x02\x02\u0952\u0957\x05\xC0a\x02\u0953\u0954\x07\x05\x02" +
		"\x02\u0954\u0956\x05\xC0a\x02\u0955\u0953\x03\x02\x02\x02\u0956\u0959" +
		"\x03\x02\x02\x02\u0957\u0955\x03\x02\x02\x02\u0957\u0958\x03\x02\x02\x02" +
		"\u0958\u095A\x03\x02\x02\x02\u0959\u0957\x03\x02\x02\x02\u095A\u095B\x07" +
		"\x04\x02\x02\u095B\u0998\x03\x02\x02\x02\u095C\u095E\x07\x97\x02\x02\u095D" +
		"\u095C\x03\x02\x02\x02\u095D\u095E\x03\x02\x02\x02\u095E\u095F\x03\x02" +
		"\x02\x02\u095F\u0960\x07q\x02\x02\u0960\u0961\x07\x03\x02\x02\u0961\u0962" +
		"\x05$\x13\x02\u0962\u0963\x07\x04\x02\x02\u0963\u0998\x03\x02\x02\x02" +
		"\u0964\u0966\x07\x97\x02\x02\u0965\u0964\x03\x02\x02\x02\u0965\u0966\x03" +
		"\x02\x02\x02\u0966\u0967\x03\x02\x02\x02\u0967\u0968\x07\xC3\x02\x02\u0968" +
		"\u0998\x05\xC6d\x02\u0969\u096B\x07\x97\x02\x02\u096A\u0969\x03\x02\x02" +
		"\x02\u096A\u096B\x03\x02\x02\x02\u096B\u096C\x03\x02\x02\x02\u096C\u096D" +
		"\x07\x84\x02\x02\u096D\u097B\t\x16\x02\x02\u096E\u096F\x07\x03\x02\x02" +
		"\u096F\u097C\x07\x04\x02\x02\u0970\u0971\x07\x03\x02\x02\u0971\u0976\x05" +
		"\xC0a\x02\u0972\u0973\x07\x05\x02\x02\u0973\u0975\x05\xC0a\x02\u0974\u0972" +
		"\x03\x02\x02\x02\u0975\u0978\x03\x02\x02\x02\u0976\u0974\x03\x02\x02\x02" +
		"\u0976\u0977\x03\x02\x02\x02\u0977\u0979\x03\x02\x02\x02\u0978\u0976\x03" +
		"\x02\x02\x02\u0979\u097A\x07\x04\x02\x02\u097A\u097C\x03\x02\x02\x02\u097B" +
		"\u096E\x03\x02\x02\x02\u097B\u0970\x03\x02\x02\x02\u097C\u0998\x03\x02" +
		"\x02\x02\u097D\u097F\x07\x97\x02\x02\u097E\u097D\x03\x02\x02\x02\u097E" +
		"\u097F\x03\x02\x02\x02\u097F\u0980\x03\x02\x02\x02\u0980\u0981\x07\x84" +
		"\x02\x02\u0981\u0984\x05\xC6d\x02\u0982\u0983\x07P\x02\x02\u0983\u0985" +
		"\x07\u0119\x02\x02\u0984\u0982\x03\x02\x02\x02\u0984\u0985\x03\x02\x02" +
		"\x02\u0985\u0998\x03\x02\x02\x02\u0986\u0988\x07{\x02\x02\u0987\u0989" +
		"\x07\x97\x02\x02\u0988\u0987\x03\x02\x02\x02\u0988\u0989\x03\x02\x02\x02" +
		"\u0989\u098A\x03\x02\x02\x02\u098A\u0998\x07\x98\x02\x02\u098B\u098D\x07" +
		"{\x02\x02\u098C\u098E\x07\x97\x02\x02\u098D\u098C\x03\x02\x02\x02\u098D" +
		"\u098E\x03\x02\x02\x02\u098E\u098F\x03\x02\x02\x02\u098F\u0998\t\x17\x02" +
		"\x02\u0990\u0992\x07{\x02\x02\u0991\u0993\x07\x97\x02\x02\u0992\u0991" +
		"\x03\x02\x02\x02\u0992\u0993\x03\x02\x02\x02\u0993\u0994\x03\x02\x02\x02" +
		"\u0994\u0995\x07J\x02\x02\u0995\u0996\x07e\x02\x02\u0996\u0998\x05\xC6" +
		"d\x02\u0997\u0946\x03\x02\x02\x02\u0997\u094E\x03\x02\x02\x02\u0997\u095D" +
		"\x03\x02\x02\x02\u0997\u0965\x03\x02\x02\x02\u0997\u096A\x03\x02\x02\x02" +
		"\u0997\u097E\x03\x02\x02\x02\u0997\u0986\x03\x02\x02\x02\u0997\u098B\x03" +
		"\x02\x02\x02\u0997\u0990\x03\x02\x02\x02\u0998\xC5\x03\x02\x02\x02\u0999" +
		"\u099A\bd\x01\x02\u099A\u099E\x05\xC8e\x02\u099B\u099C\t\x18\x02\x02\u099C" +
		"\u099E\x05\xC6d\t\u099D\u0999\x03\x02\x02\x02\u099D\u099B\x03\x02\x02" +
		"\x02\u099E\u09B4\x03\x02\x02\x02\u099F\u09A0\f\b\x02\x02\u09A0\u09A1\t" +
		"\x19\x02\x02\u09A1\u09B3\x05\xC6d\t\u09A2\u09A3\f\x07\x02\x02\u09A3\u09A4" +
		"\t\x1A\x02\x02\u09A4\u09B3\x05\xC6d\b\u09A5\u09A6\f\x06\x02\x02\u09A6" +
		"\u09A7\x07\u0114\x02\x02\u09A7\u09B3\x05\xC6d\x07\u09A8\u09A9\f\x05\x02" +
		"\x02\u09A9\u09AA\x07\u0117\x02\x02\u09AA\u09B3\x05\xC6d\x06\u09AB\u09AC" +
		"\f\x04\x02\x02\u09AC\u09AD\x07\u0115\x02\x02\u09AD\u09B3\x05\xC6d\x05" +
		"\u09AE\u09AF\f\x03\x02\x02\u09AF\u09B0\x05\xCCg\x02\u09B0\u09B1\x05\xC6" +
		"d\x04\u09B1\u09B3\x03\x02\x02\x02\u09B2\u099F\x03\x02\x02\x02\u09B2\u09A2" +
		"\x03\x02\x02\x02\u09B2\u09A5\x03\x02\x02\x02\u09B2\u09A8\x03\x02\x02\x02" +
		"\u09B2\u09AB\x03\x02\x02\x02\u09B2\u09AE\x03\x02\x02\x02\u09B3\u09B6\x03" +
		"\x02\x02\x02\u09B4\u09B2\x03\x02\x02\x02\u09B4\u09B5\x03\x02\x02\x02\u09B5" +
		"\xC7\x03\x02\x02\x02\u09B6\u09B4\x03\x02\x02\x02\u09B7\u09B8\be\x01\x02" +
		"\u09B8\u0A70\t\x1B\x02\x02\u09B9\u09BB\x07\"\x02\x02\u09BA\u09BC\x05\xF0" +
		"y\x02\u09BB\u09BA\x03\x02\x02\x02\u09BC\u09BD\x03\x02\x02\x02\u09BD\u09BB" +
		"\x03\x02\x02\x02\u09BD\u09BE\x03\x02\x02\x02\u09BE\u09C1\x03\x02\x02\x02" +
		"\u09BF\u09C0\x07N\x02\x02\u09C0\u09C2\x05\xC0a\x02\u09C1\u09BF\x03\x02" +
		"\x02\x02\u09C1\u09C2\x03\x02\x02\x02\u09C2\u09C3\x03\x02\x02\x02\u09C3" +
		"\u09C4\x07O\x02\x02\u09C4\u0A70\x03\x02\x02\x02\u09C5\u09C6\x07\"\x02" +
		"\x02\u09C6\u09C8\x05\xC0a\x02\u09C7\u09C9\x05\xF0y\x02\u09C8\u09C7\x03" +
		"\x02\x02\x02\u09C9\u09CA\x03\x02\x02\x02\u09CA\u09C8\x03\x02\x02\x02\u09CA" +
		"\u09CB\x03\x02\x02\x02\u09CB\u09CE\x03\x02\x02\x02\u09CC\u09CD\x07N\x02" +
		"\x02\u09CD\u09CF\x05\xC0a\x02\u09CE\u09CC\x03\x02\x02\x02\u09CE\u09CF" +
		"\x03\x02\x02\x02\u09CF\u09D0\x03\x02\x02\x02\u09D0\u09D1\x07O\x02\x02" +
		"\u09D1\u0A70\x03\x02\x02\x02\u09D2\u09D3\x07#\x02\x02\u09D3\u09D4\x07" +
		"\x03\x02\x02\u09D4\u09D5\x05\xC0a\x02\u09D5\u09D6\x07\x17\x02\x02\u09D6" +
		"\u09D7\x05\xE2r\x02\u09D7\u09D8\x07\x04\x02\x02\u09D8\u0A70\x03\x02\x02" +
		"\x02\u09D9\u09DA\x07\xDD\x02\x02\u09DA\u09E3\x07\x03\x02\x02\u09DB\u09E0" +
		"\x05\xB6\\\x02\u09DC\u09DD\x07\x05\x02\x02\u09DD\u09DF\x05\xB6\\\x02\u09DE" +
		"\u09DC\x03\x02\x02\x02\u09DF\u09E2\x03\x02\x02\x02\u09E0\u09DE\x03\x02" +
		"\x02\x02\u09E0\u09E1\x03\x02\x02\x02\u09E1\u09E4\x03\x02\x02\x02\u09E2" +
		"\u09E0\x03\x02\x02\x02\u09E3\u09DB\x03\x02\x02\x02\u09E3\u09E4\x03\x02" +
		"\x02\x02\u09E4\u09E5\x03\x02\x02\x02\u09E5\u0A70\x07\x04\x02\x02\u09E6" +
		"\u09E7\x07_\x02\x02\u09E7\u09E8\x07\x03\x02\x02\u09E8\u09EB\x05\xC0a\x02" +
		"\u09E9\u09EA\x07o\x02\x02\u09EA\u09EC\x07\x99\x02\x02\u09EB\u09E9\x03" +
		"\x02\x02\x02\u09EB\u09EC\x03\x02\x02\x02\u09EC\u09ED\x03\x02\x02\x02\u09ED" +
		"\u09EE\x07\x04\x02\x02\u09EE\u0A70\x03\x02\x02\x02\u09EF\u09F0\x07\x7F" +
		"\x02\x02\u09F0\u09F1\x07\x03\x02\x02\u09F1\u09F4\x05\xC0a\x02\u09F2\u09F3" +
		"\x07o\x02\x02\u09F3\u09F5\x07\x99\x02\x02\u09F4\u09F2\x03\x02\x02\x02" +
		"\u09F4\u09F5\x03\x02\x02\x02\u09F5\u09F6\x03\x02\x02\x02\u09F6\u09F7\x07" +
		"\x04\x02\x02\u09F7\u0A70\x03\x02\x02\x02\u09F8\u09F9\x07\xAE\x02\x02\u09F9" +
		"\u09FA\x07\x03\x02\x02\u09FA\u09FB\x05\xC6d\x02\u09FB\u09FC\x07q\x02\x02" +
		"\u09FC\u09FD\x05\xC6d\x02\u09FD\u09FE\x07\x04\x02\x02\u09FE\u0A70\x03" +
		"\x02\x02\x02\u09FF\u0A70\x05\xCAf\x02\u0A00\u0A70\x07\u0110\x02\x02\u0A01" +
		"\u0A02\x05\u0100\x81\x02\u0A02\u0A03\x07\x06\x02\x02\u0A03\u0A04\x07\u0110" +
		"\x02\x02\u0A04\u0A70\x03\x02\x02\x02\u0A05\u0A06\x07\x03\x02\x02\u0A06" +
		"\u0A09\x05\xB6\\\x02\u0A07\u0A08\x07\x05\x02\x02\u0A08\u0A0A\x05\xB6\\" +
		"\x02\u0A09\u0A07\x03\x02\x02\x02\u0A0A\u0A0B\x03\x02\x02\x02\u0A0B\u0A09" +
		"\x03\x02\x02\x02\u0A0B\u0A0C\x03\x02\x02\x02\u0A0C\u0A0D\x03\x02\x02\x02" +
		"\u0A0D\u0A0E\x07\x04\x02\x02\u0A0E\u0A70\x03\x02\x02\x02\u0A0F\u0A10\x07" +
		"\x03\x02\x02\u0A10\u0A11\x05$\x13\x02\u0A11\u0A12\x07\x04\x02\x02\u0A12" +
		"\u0A70\x03\x02\x02\x02\u0A13\u0A14\x05\xFE\x80\x02\u0A14\u0A20\x07\x03" +
		"\x02\x02\u0A15\u0A17\x05\x8CG\x02\u0A16\u0A15\x03\x02\x02\x02\u0A16\u0A17" +
		"\x03\x02\x02\x02\u0A17\u0A18\x03\x02\x02\x02\u0A18\u0A1D\x05\xC0a\x02" +
		"\u0A19\u0A1A\x07\x05\x02\x02\u0A1A\u0A1C\x05\xC0a\x02\u0A1B\u0A19\x03" +
		"\x02\x02\x02\u0A1C\u0A1F\x03\x02\x02\x02\u0A1D\u0A1B\x03\x02\x02\x02\u0A1D" +
		"\u0A1E\x03\x02\x02\x02\u0A1E\u0A21\x03\x02\x02\x02\u0A1F\u0A1D\x03\x02" +
		"\x02\x02\u0A20\u0A16\x03\x02\x02\x02\u0A20\u0A21\x03\x02\x02\x02";
	private static readonly _serializedATNSegment5: string =
		"\u0A21\u0A22\x03\x02\x02\x02\u0A22\u0A29\x07\x04\x02\x02\u0A23\u0A24\x07" +
		"]\x02\x02\u0A24\u0A25\x07\x03\x02\x02\u0A25\u0A26\x07\u0102\x02\x02\u0A26" +
		"\u0A27\x05\xC2b\x02\u0A27\u0A28\x07\x04\x02\x02\u0A28\u0A2A\x03\x02\x02" +
		"\x02\u0A29\u0A23\x03\x02\x02\x02\u0A29\u0A2A\x03\x02\x02\x02\u0A2A\u0A2D" +
		"\x03\x02\x02\x02\u0A2B\u0A2C\x07\xA4\x02\x02\u0A2C\u0A2E\x05\xF6|\x02" +
		"\u0A2D\u0A2B\x03\x02\x02\x02\u0A2D\u0A2E\x03\x02\x02\x02\u0A2E\u0A70\x03" +
		"\x02\x02\x02\u0A2F\u0A30\x05\u0106\x84\x02\u0A30\u0A31\x07\t\x02\x02\u0A31" +
		"\u0A32\x05\xC0a\x02\u0A32\u0A70\x03\x02\x02\x02\u0A33\u0A34\x07\x03\x02" +
		"\x02\u0A34\u0A37\x05\u0106\x84\x02\u0A35\u0A36\x07\x05\x02\x02\u0A36\u0A38" +
		"\x05\u0106\x84\x02\u0A37\u0A35\x03\x02\x02\x02\u0A38\u0A39\x03\x02\x02" +
		"\x02\u0A39\u0A37\x03\x02\x02\x02\u0A39\u0A3A\x03\x02\x02\x02\u0A3A\u0A3B" +
		"\x03\x02\x02\x02\u0A3B\u0A3C\x07\x04\x02\x02\u0A3C\u0A3D\x07\t\x02\x02" +
		"\u0A3D\u0A3E\x05\xC0a\x02\u0A3E\u0A70\x03\x02\x02\x02\u0A3F\u0A70\x05" +
		"\u0106\x84\x02\u0A40\u0A41\x07\x03\x02\x02\u0A41\u0A42\x05\xC0a\x02\u0A42" +
		"\u0A43\x07\x04\x02\x02\u0A43\u0A70\x03\x02\x02\x02\u0A44\u0A45\x07Y\x02" +
		"\x02\u0A45\u0A46\x07\x03\x02\x02\u0A46\u0A47\x05\u0106\x84\x02\u0A47\u0A48" +
		"\x07e\x02\x02\u0A48\u0A49\x05\xC6d\x02\u0A49\u0A4A\x07\x04\x02\x02\u0A4A" +
		"\u0A70\x03\x02\x02\x02\u0A4B\u0A4C\t\x1C\x02\x02\u0A4C\u0A4D\x07\x03\x02" +
		"\x02\u0A4D\u0A4E\x05\xC6d\x02\u0A4E\u0A4F\t\x1D\x02\x02\u0A4F\u0A52\x05" +
		"\xC6d\x02\u0A50\u0A51\t\x1E\x02\x02\u0A51\u0A53\x05\xC6d\x02\u0A52\u0A50" +
		"\x03\x02\x02\x02\u0A52\u0A53\x03\x02\x02\x02\u0A53\u0A54\x03\x02\x02\x02" +
		"\u0A54\u0A55\x07\x04\x02\x02\u0A55\u0A70\x03\x02\x02\x02\u0A56\u0A57\x07" +
		"\xEE\x02\x02\u0A57\u0A59\x07\x03\x02\x02\u0A58\u0A5A\t\x1F\x02\x02\u0A59" +
		"\u0A58\x03\x02\x02\x02\u0A59\u0A5A\x03\x02\x02\x02\u0A5A\u0A5C\x03\x02" +
		"\x02\x02\u0A5B\u0A5D\x05\xC6d\x02\u0A5C\u0A5B\x03\x02\x02\x02\u0A5C\u0A5D" +
		"\x03\x02\x02\x02\u0A5D\u0A5E\x03\x02\x02\x02\u0A5E\u0A5F\x07e\x02\x02" +
		"\u0A5F\u0A60\x05\xC6d\x02\u0A60\u0A61\x07\x04\x02\x02\u0A61\u0A70\x03" +
		"\x02\x02\x02\u0A62\u0A63\x07\xA6\x02\x02\u0A63\u0A64\x07\x03\x02\x02\u0A64" +
		"\u0A65\x05\xC6d\x02\u0A65\u0A66\x07\xAD\x02\x02\u0A66\u0A67\x05\xC6d\x02" +
		"\u0A67\u0A68\x07e\x02\x02\u0A68\u0A6B\x05\xC6d\x02\u0A69\u0A6A\x07a\x02" +
		"\x02\u0A6A\u0A6C\x05\xC6d\x02\u0A6B\u0A69\x03\x02\x02\x02\u0A6B\u0A6C" +
		"\x03\x02\x02\x02\u0A6C\u0A6D\x03\x02\x02\x02\u0A6D\u0A6E\x07\x04\x02\x02" +
		"\u0A6E\u0A70\x03\x02\x02\x02\u0A6F\u09B7\x03\x02\x02\x02\u0A6F\u09B9\x03" +
		"\x02\x02\x02\u0A6F\u09C5\x03\x02\x02\x02\u0A6F\u09D2\x03\x02\x02\x02\u0A6F" +
		"\u09D9\x03\x02\x02\x02\u0A6F\u09E6\x03\x02\x02\x02\u0A6F\u09EF\x03\x02" +
		"\x02\x02\u0A6F\u09F8\x03\x02\x02\x02\u0A6F\u09FF\x03\x02\x02\x02\u0A6F" +
		"\u0A00\x03\x02\x02\x02\u0A6F\u0A01\x03\x02\x02\x02\u0A6F\u0A05\x03\x02" +
		"\x02\x02\u0A6F\u0A0F\x03\x02\x02\x02\u0A6F\u0A13\x03\x02\x02\x02\u0A6F" +
		"\u0A2F\x03\x02\x02\x02\u0A6F\u0A33\x03\x02\x02\x02\u0A6F\u0A3F\x03\x02" +
		"\x02\x02\u0A6F\u0A40\x03\x02\x02\x02\u0A6F\u0A44\x03\x02\x02\x02\u0A6F" +
		"\u0A4B\x03\x02\x02\x02\u0A6F\u0A56\x03\x02\x02\x02\u0A6F\u0A62\x03\x02" +
		"\x02\x02\u0A70\u0A7B\x03\x02\x02\x02\u0A71\u0A72\f\n\x02\x02\u0A72\u0A73" +
		"\x07\n\x02\x02\u0A73\u0A74\x05\xC6d\x02\u0A74\u0A75\x07\v\x02\x02\u0A75" +
		"\u0A7A\x03\x02\x02\x02\u0A76\u0A77\f\b\x02\x02\u0A77\u0A78\x07\x06\x02" +
		"\x02\u0A78\u0A7A\x05\u0106\x84\x02\u0A79\u0A71\x03\x02\x02\x02\u0A79\u0A76" +
		"\x03\x02\x02\x02\u0A7A\u0A7D\x03\x02\x02\x02\u0A7B\u0A79\x03\x02\x02\x02" +
		"\u0A7B\u0A7C\x03\x02\x02\x02\u0A7C\xC9\x03\x02\x02\x02\u0A7D\u0A7B\x03" +
		"\x02\x02\x02\u0A7E\u0A8B\x07\x98\x02\x02\u0A7F\u0A8B\x05\xD4k\x02\u0A80" +
		"\u0A81\x05\u0106\x84\x02\u0A81\u0A82\x07\u0119\x02\x02\u0A82\u0A8B\x03" +
		"\x02\x02\x02\u0A83\u0A8B\x05\u010C\x87\x02\u0A84\u0A8B\x05\xD2j\x02\u0A85" +
		"\u0A87\x07\u0119\x02\x02\u0A86\u0A85\x03\x02\x02\x02\u0A87\u0A88\x03\x02" +
		"\x02\x02\u0A88\u0A86\x03\x02\x02\x02\u0A88\u0A89\x03\x02\x02\x02\u0A89" +
		"\u0A8B\x03\x02\x02\x02\u0A8A\u0A7E\x03\x02\x02\x02\u0A8A\u0A7F\x03\x02" +
		"\x02\x02\u0A8A\u0A80\x03\x02\x02\x02\u0A8A\u0A83\x03\x02\x02\x02\u0A8A" +
		"\u0A84\x03\x02\x02\x02\u0A8A\u0A86\x03\x02\x02\x02\u0A8B\xCB\x03\x02\x02" +
		"\x02\u0A8C\u0A8D\t \x02\x02\u0A8D\xCD\x03\x02\x02\x02\u0A8E\u0A8F\t!\x02" +
		"\x02\u0A8F\xCF\x03\x02\x02\x02\u0A90\u0A91\t\"\x02\x02\u0A91\xD1\x03\x02" +
		"\x02\x02\u0A92\u0A93\t#\x02\x02\u0A93\xD3\x03\x02\x02\x02\u0A94\u0A97" +
		"\x07y\x02\x02\u0A95\u0A98\x05\xD6l\x02\u0A96\u0A98\x05\xDAn\x02\u0A97" +
		"\u0A95\x03\x02\x02\x02\u0A97\u0A96\x03\x02\x02\x02\u0A97\u0A98\x03\x02" +
		"\x02\x02\u0A98\xD5\x03\x02\x02\x02\u0A99\u0A9B\x05\xD8m\x02\u0A9A\u0A9C" +
		"\x05\xDCo\x02\u0A9B\u0A9A\x03\x02\x02\x02\u0A9B\u0A9C\x03\x02\x02\x02" +
		"\u0A9C\xD7\x03\x02\x02\x02\u0A9D\u0A9E\x05\xDEp\x02\u0A9E\u0A9F\x05\u0106" +
		"\x84\x02\u0A9F\u0AA1\x03\x02\x02\x02\u0AA0\u0A9D\x03\x02\x02\x02\u0AA1" +
		"\u0AA2\x03\x02\x02\x02\u0AA2\u0AA0\x03\x02\x02\x02\u0AA2\u0AA3\x03\x02" +
		"\x02\x02\u0AA3\xD9\x03\x02\x02\x02\u0AA4\u0AA7\x05\xDCo\x02\u0AA5\u0AA8" +
		"\x05\xD8m\x02\u0AA6\u0AA8\x05\xDCo\x02\u0AA7\u0AA5\x03\x02\x02\x02\u0AA7" +
		"\u0AA6\x03\x02\x02\x02\u0AA7\u0AA8\x03\x02\x02\x02\u0AA8\xDB\x03\x02\x02" +
		"\x02\u0AA9\u0AAA\x05\xDEp\x02\u0AAA\u0AAB\x05\u0106\x84\x02\u0AAB\u0AAC" +
		"\x07\xE8\x02\x02\u0AAC\u0AAD\x05\u0106\x84\x02\u0AAD\xDD\x03\x02\x02\x02" +
		"\u0AAE\u0AB0\t$\x02\x02\u0AAF\u0AAE\x03\x02\x02\x02\u0AAF\u0AB0\x03\x02" +
		"\x02\x02\u0AB0\u0AB1\x03\x02\x02\x02\u0AB1\u0AB4\t\x15\x02\x02\u0AB2\u0AB4" +
		"\x07\u0119\x02\x02\u0AB3\u0AAF\x03\x02\x02\x02\u0AB3\u0AB2\x03\x02\x02" +
		"\x02\u0AB4\xDF\x03\x02\x02\x02\u0AB5\u0AB9\x07_\x02\x02\u0AB6\u0AB7\x07" +
		"\x0E\x02\x02\u0AB7\u0AB9\x05\u0102\x82\x02\u0AB8\u0AB5\x03\x02\x02\x02" +
		"\u0AB8\u0AB6\x03\x02\x02\x02\u0AB9\xE1\x03\x02\x02\x02\u0ABA\u0ABB\x07" +
		"\x16\x02\x02\u0ABB\u0ABC\x07\u010A\x02\x02\u0ABC\u0ABD\x05\xE2r\x02\u0ABD" +
		"\u0ABE\x07\u010C\x02\x02\u0ABE\u0ADD\x03\x02\x02\x02\u0ABF\u0AC0\x07\x8F" +
		"\x02\x02\u0AC0\u0AC1\x07\u010A\x02\x02\u0AC1\u0AC2\x05\xE2r\x02\u0AC2" +
		"\u0AC3\x07\x05\x02\x02\u0AC3\u0AC4\x05\xE2r\x02\u0AC4\u0AC5\x07\u010C" +
		"\x02\x02\u0AC5\u0ADD\x03\x02\x02\x02\u0AC6\u0ACD\x07\xDD\x02\x02\u0AC7" +
		"\u0AC9\x07\u010A\x02\x02\u0AC8\u0ACA\x05\xECw\x02\u0AC9\u0AC8\x03\x02" +
		"\x02\x02\u0AC9\u0ACA\x03\x02\x02\x02\u0ACA\u0ACB\x03\x02\x02\x02\u0ACB" +
		"\u0ACE\x07\u010C\x02\x02\u0ACC\u0ACE\x07\u0108\x02\x02\u0ACD\u0AC7\x03" +
		"\x02\x02\x02\u0ACD\u0ACC\x03\x02\x02\x02\u0ACE\u0ADD\x03\x02\x02\x02\u0ACF" +
		"\u0ADA\x05\u0106\x84\x02\u0AD0\u0AD1\x07\x03\x02\x02\u0AD1\u0AD6\x07\u011D" +
		"\x02\x02\u0AD2\u0AD3\x07\x05\x02\x02\u0AD3\u0AD5\x07\u011D\x02\x02\u0AD4" +
		"\u0AD2\x03\x02\x02\x02\u0AD5\u0AD8\x03\x02\x02\x02\u0AD6\u0AD4\x03\x02" +
		"\x02\x02\u0AD6\u0AD7\x03\x02\x02\x02\u0AD7\u0AD9\x03\x02\x02\x02\u0AD8" +
		"\u0AD6\x03\x02\x02\x02\u0AD9\u0ADB\x07\x04\x02\x02\u0ADA\u0AD0\x03\x02" +
		"\x02\x02\u0ADA\u0ADB\x03\x02\x02\x02\u0ADB\u0ADD\x03\x02\x02\x02\u0ADC" +
		"\u0ABA\x03\x02\x02\x02\u0ADC\u0ABF\x03\x02\x02\x02\u0ADC\u0AC6\x03\x02" +
		"\x02\x02\u0ADC\u0ACF\x03\x02\x02\x02\u0ADD\xE3\x03\x02\x02\x02\u0ADE\u0AE3" +
		"\x05\xE6t\x02\u0ADF\u0AE0\x07\x05\x02\x02\u0AE0\u0AE2\x05\xE6t\x02\u0AE1" +
		"\u0ADF\x03\x02\x02\x02\u0AE2\u0AE5\x03\x02\x02\x02\u0AE3\u0AE1\x03\x02" +
		"\x02\x02\u0AE3\u0AE4\x03\x02\x02\x02\u0AE4\xE5\x03\x02\x02\x02\u0AE5\u0AE3" +
		"\x03\x02\x02\x02\u0AE6\u0AE7\x05\xB2Z\x02\u0AE7\u0AEA\x05\xE2r\x02\u0AE8" +
		"\u0AE9\x07\x97\x02\x02\u0AE9\u0AEB\x07\x98\x02\x02\u0AEA\u0AE8\x03\x02" +
		"\x02\x02\u0AEA\u0AEB\x03\x02\x02\x02\u0AEB\u0AED\x03\x02\x02\x02\u0AEC" +
		"\u0AEE\x05\"\x12\x02\u0AED\u0AEC\x03\x02\x02\x02\u0AED\u0AEE\x03\x02\x02" +
		"\x02\u0AEE\u0AF0\x03\x02\x02\x02\u0AEF\u0AF1\x05\xE0q\x02\u0AF0\u0AEF" +
		"\x03\x02\x02\x02\u0AF0\u0AF1\x03\x02\x02\x02\u0AF1\xE7\x03\x02\x02\x02" +
		"\u0AF2\u0AF7\x05\xEAv\x02\u0AF3\u0AF4\x07\x05\x02\x02\u0AF4\u0AF6\x05" +
		"\xEAv\x02\u0AF5\u0AF3\x03\x02\x02\x02\u0AF6\u0AF9\x03\x02\x02\x02\u0AF7" +
		"\u0AF5\x03\x02\x02\x02\u0AF7\u0AF8\x03\x02\x02\x02\u0AF8\xE9\x03\x02\x02" +
		"\x02\u0AF9\u0AF7\x03\x02\x02\x02\u0AFA\u0AFB\x05\u0102\x82\x02\u0AFB\u0AFE" +
		"\x05\xE2r\x02\u0AFC\u0AFD\x07\x97\x02\x02\u0AFD\u0AFF\x07\x98\x02\x02" +
		"\u0AFE\u0AFC\x03\x02\x02\x02\u0AFE\u0AFF\x03\x02\x02\x02\u0AFF\u0B01\x03" +
		"\x02\x02\x02\u0B00\u0B02\x05\"\x12\x02\u0B01\u0B00\x03\x02\x02\x02\u0B01" +
		"\u0B02\x03\x02\x02\x02\u0B02\xEB\x03\x02\x02\x02\u0B03\u0B08\x05\xEEx" +
		"\x02\u0B04\u0B05\x07\x05\x02\x02\u0B05\u0B07\x05\xEEx\x02\u0B06\u0B04" +
		"\x03\x02\x02\x02\u0B07\u0B0A\x03\x02\x02\x02\u0B08\u0B06\x03\x02\x02\x02" +
		"\u0B08\u0B09\x03\x02\x02\x02\u0B09\xED\x03\x02\x02\x02\u0B0A\u0B08\x03" +
		"\x02\x02\x02\u0B0B\u0B0C\x05\u0106\x84\x02\u0B0C\u0B0D\x07\f\x02\x02\u0B0D" +
		"\u0B10\x05\xE2r\x02\u0B0E\u0B0F\x07\x97\x02\x02\u0B0F\u0B11\x07\x98\x02" +
		"\x02\u0B10\u0B0E\x03\x02\x02\x02\u0B10\u0B11\x03\x02\x02\x02\u0B11\u0B13" +
		"\x03\x02\x02\x02\u0B12\u0B14\x05\"\x12\x02\u0B13\u0B12\x03\x02\x02\x02" +
		"\u0B13\u0B14\x03\x02\x02\x02\u0B14\xEF\x03\x02\x02\x02\u0B15\u0B16\x07" +
		"\u0101\x02\x02\u0B16\u0B17\x05\xC0a\x02\u0B17\u0B18\x07\xE6\x02\x02\u0B18" +
		"\u0B19\x05\xC0a\x02\u0B19\xF1\x03\x02\x02\x02\u0B1A\u0B1B\x07\u0103\x02" +
		"\x02\u0B1B\u0B20\x05\xF4{\x02\u0B1C\u0B1D\x07\x05\x02\x02\u0B1D\u0B1F" +
		"\x05\xF4{\x02\u0B1E\u0B1C\x03\x02\x02\x02\u0B1F\u0B22\x03\x02\x02\x02" +
		"\u0B20\u0B1E\x03\x02\x02\x02\u0B20\u0B21\x03\x02\x02\x02\u0B21\xF3\x03" +
		"\x02\x02\x02\u0B22\u0B20\x03\x02\x02\x02\u0B23\u0B24\x05\u0102\x82\x02" +
		"\u0B24\u0B25\x07\x17\x02\x02\u0B25\u0B26\x05\xF6|\x02\u0B26\xF5\x03\x02" +
		"\x02\x02\u0B27\u0B56\x05\u0102\x82\x02\u0B28\u0B29\x07\x03\x02\x02\u0B29" +
		"\u0B2A\x05\u0102\x82\x02\u0B2A\u0B2B\x07\x04\x02\x02\u0B2B\u0B56\x03\x02" +
		"\x02\x02\u0B2C\u0B4F\x07\x03\x02\x02\u0B2D\u0B2E\x07\'\x02\x02\u0B2E\u0B2F" +
		"\x07\x1F\x02\x02\u0B2F\u0B34\x05\xC0a\x02\u0B30\u0B31\x07\x05\x02\x02" +
		"\u0B31\u0B33\x05\xC0a\x02\u0B32\u0B30\x03\x02\x02\x02\u0B33\u0B36\x03" +
		"\x02\x02\x02\u0B34\u0B32\x03\x02\x02\x02\u0B34\u0B35\x03\x02\x02\x02\u0B35" +
		"\u0B50\x03\x02\x02\x02\u0B36\u0B34\x03\x02\x02\x02\u0B37\u0B38\t%\x02" +
		"\x02\u0B38\u0B39\x07\x1F\x02\x02\u0B39\u0B3E\x05\xC0a\x02\u0B3A\u0B3B" +
		"\x07\x05\x02\x02\u0B3B\u0B3D\x05\xC0a\x02\u0B3C\u0B3A\x03\x02\x02\x02" +
		"\u0B3D\u0B40\x03\x02\x02\x02\u0B3E\u0B3C\x03\x02\x02\x02\u0B3E\u0B3F\x03" +
		"\x02\x02\x02\u0B3F\u0B42\x03\x02\x02\x02\u0B40\u0B3E\x03\x02\x02\x02\u0B41" +
		"\u0B37\x03\x02\x02\x02\u0B41\u0B42\x03\x02\x02\x02\u0B42\u0B4D\x03\x02" +
		"\x02\x02\u0B43\u0B44\t&\x02\x02\u0B44\u0B45\x07\x1F\x02\x02\u0B45\u0B4A" +
		"\x05Z.\x02\u0B46\u0B47\x07\x05\x02\x02\u0B47\u0B49\x05Z.\x02\u0B48\u0B46" +
		"\x03\x02\x02\x02\u0B49\u0B4C\x03\x02\x02\x02\u0B4A\u0B48\x03\x02\x02\x02" +
		"\u0B4A\u0B4B\x03\x02\x02\x02\u0B4B\u0B4E\x03\x02\x02\x02\u0B4C\u0B4A\x03" +
		"\x02\x02\x02\u0B4D\u0B43\x03\x02\x02\x02\u0B4D\u0B4E\x03\x02\x02\x02\u0B4E" +
		"\u0B50\x03\x02\x02\x02\u0B4F\u0B2D\x03\x02\x02\x02\u0B4F\u0B41\x03\x02" +
		"\x02\x02\u0B50\u0B52\x03\x02\x02\x02\u0B51\u0B53\x05\xF8}\x02\u0B52\u0B51" +
		"\x03\x02\x02\x02\u0B52\u0B53\x03\x02\x02\x02\u0B53\u0B54\x03\x02\x02\x02" +
		"\u0B54\u0B56\x07\x04\x02\x02\u0B55\u0B27\x03\x02\x02\x02\u0B55\u0B28\x03" +
		"\x02\x02\x02\u0B55\u0B2C\x03\x02\x02\x02\u0B56\xF7\x03\x02\x02\x02\u0B57" +
		"\u0B58\x07\xB5\x02\x02\u0B58\u0B68\x05\xFA~\x02\u0B59\u0B5A\x07\xC9\x02" +
		"\x02\u0B5A\u0B68\x05\xFA~\x02\u0B5B\u0B5C\x07\xB5\x02\x02\u0B5C\u0B5D" +
		"\x07\x1B\x02\x02\u0B5D\u0B5E\x05\xFA~\x02\u0B5E\u0B5F\x07\x12\x02\x02" +
		"\u0B5F\u0B60\x05\xFA~\x02\u0B60\u0B68\x03\x02\x02\x02\u0B61\u0B62\x07" +
		"\xC9\x02\x02\u0B62\u0B63\x07\x1B\x02\x02\u0B63\u0B64\x05\xFA~\x02\u0B64" +
		"\u0B65\x07\x12\x02\x02\u0B65\u0B66\x05\xFA~\x02\u0B66\u0B68\x03\x02\x02" +
		"\x02\u0B67\u0B57\x03\x02\x02\x02\u0B67\u0B59\x03\x02\x02\x02\u0B67\u0B5B" +
		"\x03\x02\x02\x02\u0B67\u0B61\x03\x02\x02\x02\u0B68\xF9\x03\x02\x02\x02" +
		"\u0B69\u0B6A\x07\xF3\x02\x02\u0B6A\u0B71\t\'\x02\x02\u0B6B\u0B6C\x079" +
		"\x02\x02\u0B6C\u0B71\x07\xC8\x02\x02\u0B6D\u0B6E\x05\xC0a\x02\u0B6E\u0B6F" +
		"\t\'\x02\x02\u0B6F\u0B71\x03\x02\x02\x02\u0B70\u0B69\x03\x02\x02\x02\u0B70" +
		"\u0B6B\x03\x02\x02\x02\u0B70\u0B6D\x03\x02\x02\x02\u0B71\xFB\x03\x02\x02" +
		"\x02\u0B72\u0B77\x05\u0100\x81\x02\u0B73\u0B74\x07\x05\x02\x02\u0B74\u0B76" +
		"\x05\u0100\x81\x02\u0B75\u0B73\x03\x02\x02\x02\u0B76\u0B79\x03\x02\x02" +
		"\x02\u0B77\u0B75\x03\x02\x02\x02\u0B77\u0B78\x03\x02\x02\x02\u0B78\xFD" +
		"\x03\x02\x02\x02\u0B79\u0B77\x03\x02\x02\x02\u0B7A\u0B7F\x05\u0100\x81" +
		"\x02\u0B7B\u0B7F\x07]\x02\x02\u0B7C\u0B7F\x07\x83\x02\x02\u0B7D\u0B7F" +
		"\x07\xC2\x02\x02\u0B7E\u0B7A\x03\x02\x02\x02\u0B7E\u0B7B\x03\x02\x02\x02" +
		"\u0B7E\u0B7C\x03\x02\x02\x02\u0B7E\u0B7D\x03\x02\x02\x02\u0B7F\xFF\x03" +
		"\x02\x02\x02\u0B80\u0B85\x05\u0106\x84\x02\u0B81\u0B82\x07\x06\x02\x02" +
		"\u0B82\u0B84\x05\u0106\x84\x02\u0B83\u0B81\x03\x02\x02\x02\u0B84\u0B87" +
		"\x03\x02\x02\x02\u0B85\u0B83\x03\x02\x02\x02\u0B85\u0B86\x03\x02\x02\x02" +
		"\u0B86\u0101\x03\x02\x02\x02\u0B87\u0B85\x03\x02\x02\x02\u0B88\u0B89\x05" +
		"\u0106\x84\x02\u0B89\u0B8A\x05\u0104\x83\x02\u0B8A\u0103\x03\x02\x02\x02" +
		"\u0B8B\u0B8C\x07\u010F\x02\x02\u0B8C\u0B8E\x05\u0106\x84\x02\u0B8D\u0B8B" +
		"\x03\x02\x02\x02\u0B8E\u0B8F\x03\x02\x02\x02\u0B8F\u0B8D\x03\x02\x02\x02" +
		"\u0B8F\u0B90\x03\x02\x02\x02\u0B90\u0B93\x03\x02\x02\x02\u0B91\u0B93\x03" +
		"\x02\x02\x02\u0B92\u0B8D\x03\x02\x02\x02\u0B92\u0B91\x03\x02\x02\x02\u0B93" +
		"\u0105\x03\x02\x02\x02\u0B94\u0B98\x05\u0108\x85\x02\u0B95\u0B96\x06\x84" +
		"\x12\x02\u0B96\u0B98\x05\u0112\x8A\x02\u0B97\u0B94\x03\x02\x02\x02\u0B97" +
		"\u0B95\x03\x02\x02\x02\u0B98\u0107\x03\x02\x02\x02\u0B99\u0BA0\x07\u0123" +
		"\x02\x02\u0B9A\u0BA0\x05\u010A\x86\x02\u0B9B\u0B9C\x06\x85\x13\x02\u0B9C" +
		"\u0BA0\x05\u0110\x89\x02\u0B9D\u0B9E\x06\x85\x14\x02\u0B9E\u0BA0\x05\u0114" +
		"\x8B\x02\u0B9F\u0B99\x03\x02\x02\x02\u0B9F\u0B9A\x03\x02\x02\x02\u0B9F" +
		"\u0B9B\x03\x02\x02\x02\u0B9F\u0B9D\x03\x02\x02\x02\u0BA0\u0109\x03\x02" +
		"\x02\x02\u0BA1\u0BA2\x07\u0124\x02\x02\u0BA2\u010B\x03\x02\x02\x02\u0BA3" +
		"\u0BA5\x06\x87\x15\x02\u0BA4\u0BA6\x07\u010F\x02\x02\u0BA5\u0BA4\x03\x02" +
		"\x02\x02\u0BA5\u0BA6\x03\x02\x02\x02\u0BA6\u0BA7\x03\x02\x02\x02\u0BA7" +
		"\u0BCF\x07\u011E\x02\x02\u0BA8\u0BAA\x06\x87\x16\x02\u0BA9\u0BAB\x07\u010F" +
		"\x02\x02\u0BAA\u0BA9\x03\x02\x02\x02\u0BAA\u0BAB\x03\x02\x02\x02\u0BAB" +
		"\u0BAC\x03\x02\x02\x02\u0BAC\u0BCF\x07\u011F\x02\x02\u0BAD\u0BAF\x06\x87" +
		"\x17\x02\u0BAE\u0BB0\x07\u010F\x02\x02\u0BAF\u0BAE\x03\x02\x02\x02\u0BAF" +
		"\u0BB0\x03\x02\x02\x02\u0BB0\u0BB1\x03\x02\x02\x02\u0BB1\u0BCF\t(\x02" +
		"\x02\u0BB2\u0BB4\x07\u010F\x02\x02\u0BB3\u0BB2\x03\x02\x02\x02\u0BB3\u0BB4" +
		"\x03\x02\x02\x02\u0BB4\u0BB5\x03\x02\x02\x02\u0BB5\u0BCF\x07\u011D\x02" +
		"\x02\u0BB6\u0BB8\x07\u010F\x02\x02\u0BB7\u0BB6\x03\x02\x02\x02\u0BB7\u0BB8" +
		"\x03\x02\x02\x02\u0BB8\u0BB9\x03\x02\x02\x02\u0BB9\u0BCF\x07\u011A\x02" +
		"\x02\u0BBA\u0BBC\x07\u010F\x02\x02\u0BBB\u0BBA\x03\x02\x02\x02\u0BBB\u0BBC" +
		"\x03\x02\x02\x02\u0BBC\u0BBD\x03\x02\x02\x02\u0BBD\u0BCF\x07\u011B\x02" +
		"\x02\u0BBE\u0BC0\x07\u010F\x02\x02\u0BBF\u0BBE\x03\x02\x02\x02\u0BBF\u0BC0" +
		"\x03\x02\x02\x02\u0BC0\u0BC1\x03\x02\x02\x02\u0BC1\u0BCF\x07\u011C\x02" +
		"\x02\u0BC2\u0BC4\x07\u010F\x02\x02\u0BC3\u0BC2\x03\x02\x02\x02\u0BC3\u0BC4" +
		"\x03\x02\x02\x02\u0BC4\u0BC5\x03\x02\x02\x02\u0BC5\u0BCF\x07\u0121\x02" +
		"\x02\u0BC6\u0BC8\x07\u010F\x02\x02\u0BC7\u0BC6\x03\x02\x02\x02\u0BC7\u0BC8" +
		"\x03\x02\x02\x02\u0BC8\u0BC9\x03\x02\x02\x02\u0BC9\u0BCF\x07\u0120\x02" +
		"\x02\u0BCA\u0BCC\x07\u010F\x02\x02\u0BCB\u0BCA\x03\x02\x02\x02\u0BCB\u0BCC" +
		"\x03\x02\x02\x02\u0BCC\u0BCD\x03\x02\x02\x02\u0BCD\u0BCF\x07\u0122\x02" +
		"\x02\u0BCE\u0BA3\x03\x02\x02\x02\u0BCE\u0BA8\x03\x02\x02\x02\u0BCE\u0BAD" +
		"\x03\x02\x02\x02\u0BCE\u0BB3\x03\x02\x02\x02\u0BCE\u0BB7\x03\x02\x02\x02" +
		"\u0BCE\u0BBB\x03\x02\x02\x02\u0BCE\u0BBF\x03\x02\x02\x02\u0BCE\u0BC3\x03" +
		"\x02\x02\x02\u0BCE\u0BC7\x03\x02\x02\x02\u0BCE\u0BCB\x03\x02\x02\x02\u0BCF" +
		"\u010D\x03\x02\x02\x02\u0BD0\u0BD1\x07\xF1\x02\x02\u0BD1\u0BD8\x05\xE2" +
		"r\x02\u0BD2\u0BD8\x05\"\x12\x02\u0BD3\u0BD8\x05\xE0q\x02\u0BD4\u0BD5\t" +
		")\x02\x02\u0BD5\u0BD6\x07\x97\x02\x02\u0BD6\u0BD8\x07\x98\x02\x02\u0BD7" +
		"\u0BD0\x03\x02\x02\x02\u0BD7\u0BD2\x03\x02\x02\x02\u0BD7\u0BD3\x03\x02" +
		"\x02\x02\u0BD7\u0BD4\x03\x02\x02\x02\u0BD8\u010F\x03\x02\x02\x02\u0BD9" +
		"\u0BDA\t*\x02\x02\u0BDA\u0111\x03\x02\x02\x02\u0BDB\u0BDC\t+\x02\x02\u0BDC" +
		"\u0113\x03\x02\x02\x02\u0BDD\u0BDE\t,\x02\x02\u0BDE\u0115\x03\x02\x02" +
		"\x02\u018D\u011B\u011E\u0120\u0136\u013B\u0143\u014B\u014D\u0161\u0165" +
		"\u016B\u016E\u0171\u0178\u017D\u0180\u0187\u0193\u019C\u019E\u01A2\u01A5" +
		"\u01AC\u01B7\u01B9\u01C1\u01C6\u01C9\u01CF\u01DA\u021A\u0223\u0227\u022D" +
		"\u0231\u0236\u023C\u0248\u0250\u0256\u0263\u0268\u0278\u027F\u0283\u0289" +
		"\u0298\u029C\u02A2\u02A8\u02AB\u02AE\u02B4\u02B8\u02C0\u02C2\u02CB\u02CE" +
		"\u02D7\u02DC\u02E2\u02E9\u02EC\u02F2\u02FD\u0300\u0304\u0309\u030E\u0315" +
		"\u0318\u031B\u0322\u0327\u0330\u0338\u033E\u0341\u0344\u034A\u034E\u0352" +
		"\u0356\u0358\u0360\u0368\u036E\u0374\u0377\u037B\u037E\u0382\u039E\u03A1" +
		"\u03A5\u03AB\u03AE\u03B1\u03B7\u03BF\u03C4\u03CA\u03D0\u03DC\u03DF\u03E6" +
		"\u03F7\u0400\u0403\u0409\u0412\u0419\u041C\u0426\u042A\u0431\u04A5\u04AD" +
		"\u04B5\u04BE\u04C8\u04CC\u04CF\u04D5\u04DB\u04E7\u04F3\u04F8\u0501\u0509" +
		"\u0510\u0512\u0517\u051B\u0520\u0525\u052A\u052D\u0532\u0536\u053B\u053D" +
		"\u0541\u054A\u0552\u055B\u0562\u056B\u0570\u0573\u0586\u0588\u0591\u0598" +
		"\u059B\u05A2\u05A6\u05AC\u05B4\u05BF\u05CA\u05D1\u05D7\u05E4\u05EB\u05F2" +
		"\u05FE\u0606\u060C\u060F\u0618\u061B\u0624\u0627\u0630\u0633\u063C\u063F" +
		"\u0642\u0647\u0649\u0655\u065C\u0663\u0666\u0668\u0674\u0678\u067C\u0682" +
		"\u0686\u068E\u0692\u0695\u0698\u069B\u069F\u06A3\u06A6\u06AA\u06AF\u06B3" +
		"\u06B6\u06B9\u06BC\u06BE\u06CA\u06CD\u06D1\u06DB\u06DF\u06E1\u06E4\u06E8" +
		"\u06EE\u06F2\u06FD\u0707\u0713\u0722\u0727\u072E\u073E\u0743\u0750\u0755" +
		"\u0760\u0766\u076A\u0775\u0784\u0789\u0795\u079A\u07A2\u07A5\u07A9\u07B7" +
		"\u07C4\u07C9\u07CD\u07D0\u07D5\u07DE\u07E1\u07E6\u07ED\u07F0\u07F8\u07FF" +
		"\u0806\u0809\u080E\u0811\u0816\u081A\u081D\u0820\u0826\u082B\u0830\u0842" +
		"\u0844\u0847\u0852\u085B\u0862\u086A\u0871\u0875\u087D\u0885\u088B\u0893" +
		"\u089F\u08A2\u08A8\u08AC\u08AE\u08B7\u08C3\u08C5\u08CC\u08D3\u08D9\u08DF" +
		"\u08E1\u08E8\u08F0\u08F6\u08FC\u0900\u0902\u0909\u0912\u091F\u0924\u0928" +
		"\u0936\u0938\u0940\u0942\u0946\u094E\u0957\u095D\u0965\u096A\u0976\u097B" +
		"\u097E\u0984\u0988\u098D\u0992\u0997\u099D\u09B2\u09B4\u09BD\u09C1\u09CA" +
		"\u09CE\u09E0\u09E3\u09EB\u09F4\u0A0B\u0A16\u0A1D\u0A20\u0A29\u0A2D\u0A39" +
		"\u0A52\u0A59\u0A5C\u0A6B\u0A6F\u0A79\u0A7B\u0A88\u0A8A\u0A97\u0A9B\u0AA2" +
		"\u0AA7\u0AAF\u0AB3\u0AB8\u0AC9\u0ACD\u0AD6\u0ADA\u0ADC\u0AE3\u0AEA\u0AED" +
		"\u0AF0\u0AF7\u0AFE\u0B01\u0B08\u0B10\u0B13\u0B20\u0B34\u0B3E\u0B41\u0B4A" +
		"\u0B4D\u0B4F\u0B52\u0B55\u0B67\u0B70\u0B77\u0B7E\u0B85\u0B8F\u0B92\u0B97" +
		"\u0B9F\u0BA5\u0BAA\u0BAF\u0BB3\u0BB7\u0BBB\u0BBF\u0BC3\u0BC7\u0BCB\u0BCE" +
		"\u0BD7";
	public static readonly _serializedATN: string = Utils.join(
		[
			SparkSqlParser._serializedATNSegment0,
			SparkSqlParser._serializedATNSegment1,
			SparkSqlParser._serializedATNSegment2,
			SparkSqlParser._serializedATNSegment3,
			SparkSqlParser._serializedATNSegment4,
			SparkSqlParser._serializedATNSegment5,
		],
		"",
	);
	public static __ATN: ATN;
	public static get _ATN(): ATN {
		if (!SparkSqlParser.__ATN) {
			SparkSqlParser.__ATN = new ATNDeserializer().deserialize(Utils.toCharArray(SparkSqlParser._serializedATN));
		}

		return SparkSqlParser.__ATN;
	}

}

export class ProgramContext extends ParserRuleContext {
	public singleStatement(): SingleStatementContext {
		return this.getRuleContext(0, SingleStatementContext);
	}
	public EOF(): TerminalNode { return this.getToken(SparkSqlParser.EOF, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_program; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterProgram) {
			listener.enterProgram(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitProgram) {
			listener.exitProgram(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitProgram) {
			return visitor.visitProgram(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class SingleStatementContext extends ParserRuleContext {
	public statement(): StatementContext[];
	public statement(i: number): StatementContext;
	public statement(i?: number): StatementContext | StatementContext[] {
		if (i === undefined) {
			return this.getRuleContexts(StatementContext);
		} else {
			return this.getRuleContext(i, StatementContext);
		}
	}
	public emptyStatement(): EmptyStatementContext[];
	public emptyStatement(i: number): EmptyStatementContext;
	public emptyStatement(i?: number): EmptyStatementContext | EmptyStatementContext[] {
		if (i === undefined) {
			return this.getRuleContexts(EmptyStatementContext);
		} else {
			return this.getRuleContext(i, EmptyStatementContext);
		}
	}
	public SEMICOLON(): TerminalNode[];
	public SEMICOLON(i: number): TerminalNode;
	public SEMICOLON(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.SEMICOLON);
		} else {
			return this.getToken(SparkSqlParser.SEMICOLON, i);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_singleStatement; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSingleStatement) {
			listener.enterSingleStatement(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSingleStatement) {
			listener.exitSingleStatement(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSingleStatement) {
			return visitor.visitSingleStatement(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class EmptyStatementContext extends ParserRuleContext {
	public SEMICOLON(): TerminalNode { return this.getToken(SparkSqlParser.SEMICOLON, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_emptyStatement; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterEmptyStatement) {
			listener.enterEmptyStatement(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitEmptyStatement) {
			listener.exitEmptyStatement(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitEmptyStatement) {
			return visitor.visitEmptyStatement(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class SingleExpressionContext extends ParserRuleContext {
	public namedExpression(): NamedExpressionContext {
		return this.getRuleContext(0, NamedExpressionContext);
	}
	public EOF(): TerminalNode { return this.getToken(SparkSqlParser.EOF, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_singleExpression; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSingleExpression) {
			listener.enterSingleExpression(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSingleExpression) {
			listener.exitSingleExpression(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSingleExpression) {
			return visitor.visitSingleExpression(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class SingleTableIdentifierContext extends ParserRuleContext {
	public tableIdentifier(): TableIdentifierContext {
		return this.getRuleContext(0, TableIdentifierContext);
	}
	public EOF(): TerminalNode { return this.getToken(SparkSqlParser.EOF, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_singleTableIdentifier; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSingleTableIdentifier) {
			listener.enterSingleTableIdentifier(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSingleTableIdentifier) {
			listener.exitSingleTableIdentifier(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSingleTableIdentifier) {
			return visitor.visitSingleTableIdentifier(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class SingleMultipartIdentifierContext extends ParserRuleContext {
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public EOF(): TerminalNode { return this.getToken(SparkSqlParser.EOF, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_singleMultipartIdentifier; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSingleMultipartIdentifier) {
			listener.enterSingleMultipartIdentifier(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSingleMultipartIdentifier) {
			listener.exitSingleMultipartIdentifier(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSingleMultipartIdentifier) {
			return visitor.visitSingleMultipartIdentifier(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class SingleDataTypeContext extends ParserRuleContext {
	public dataType(): DataTypeContext {
		return this.getRuleContext(0, DataTypeContext);
	}
	public EOF(): TerminalNode { return this.getToken(SparkSqlParser.EOF, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_singleDataType; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSingleDataType) {
			listener.enterSingleDataType(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSingleDataType) {
			listener.exitSingleDataType(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSingleDataType) {
			return visitor.visitSingleDataType(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class SingleTableSchemaContext extends ParserRuleContext {
	public colTypeList(): ColTypeListContext {
		return this.getRuleContext(0, ColTypeListContext);
	}
	public EOF(): TerminalNode { return this.getToken(SparkSqlParser.EOF, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_singleTableSchema; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSingleTableSchema) {
			listener.enterSingleTableSchema(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSingleTableSchema) {
			listener.exitSingleTableSchema(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSingleTableSchema) {
			return visitor.visitSingleTableSchema(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class StatementContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_statement; }
	public copyFrom(ctx: StatementContext): void {
		super.copyFrom(ctx);
	}
}
export class StatementDefaultContext extends StatementContext {
	public query(): QueryContext {
		return this.getRuleContext(0, QueryContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterStatementDefault) {
			listener.enterStatementDefault(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitStatementDefault) {
			listener.exitStatementDefault(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitStatementDefault) {
			return visitor.visitStatementDefault(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DmlStatementContext extends StatementContext {
	public dmlStatementNoWith(): DmlStatementNoWithContext {
		return this.getRuleContext(0, DmlStatementNoWithContext);
	}
	public ctes(): CtesContext | undefined {
		return this.tryGetRuleContext(0, CtesContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDmlStatement) {
			listener.enterDmlStatement(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDmlStatement) {
			listener.exitDmlStatement(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDmlStatement) {
			return visitor.visitDmlStatement(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class UseContext extends StatementContext {
	public USE(): TerminalNode { return this.getToken(SparkSqlParser.USE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public NAMESPACE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NAMESPACE, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterUse) {
			listener.enterUse(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitUse) {
			listener.exitUse(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitUse) {
			return visitor.visitUse(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class CreateNamespaceContext extends StatementContext {
	public CREATE(): TerminalNode { return this.getToken(SparkSqlParser.CREATE, 0); }
	public namespace(): NamespaceContext {
		return this.getRuleContext(0, NamespaceContext);
	}
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	public commentSpec(): CommentSpecContext[];
	public commentSpec(i: number): CommentSpecContext;
	public commentSpec(i?: number): CommentSpecContext | CommentSpecContext[] {
		if (i === undefined) {
			return this.getRuleContexts(CommentSpecContext);
		} else {
			return this.getRuleContext(i, CommentSpecContext);
		}
	}
	public locationSpec(): LocationSpecContext[];
	public locationSpec(i: number): LocationSpecContext;
	public locationSpec(i?: number): LocationSpecContext | LocationSpecContext[] {
		if (i === undefined) {
			return this.getRuleContexts(LocationSpecContext);
		} else {
			return this.getRuleContext(i, LocationSpecContext);
		}
	}
	public WITH(): TerminalNode[];
	public WITH(i: number): TerminalNode;
	public WITH(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.WITH);
		} else {
			return this.getToken(SparkSqlParser.WITH, i);
		}
	}
	public tablePropertyList(): TablePropertyListContext[];
	public tablePropertyList(i: number): TablePropertyListContext;
	public tablePropertyList(i?: number): TablePropertyListContext | TablePropertyListContext[] {
		if (i === undefined) {
			return this.getRuleContexts(TablePropertyListContext);
		} else {
			return this.getRuleContext(i, TablePropertyListContext);
		}
	}
	public DBPROPERTIES(): TerminalNode[];
	public DBPROPERTIES(i: number): TerminalNode;
	public DBPROPERTIES(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.DBPROPERTIES);
		} else {
			return this.getToken(SparkSqlParser.DBPROPERTIES, i);
		}
	}
	public PROPERTIES(): TerminalNode[];
	public PROPERTIES(i: number): TerminalNode;
	public PROPERTIES(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.PROPERTIES);
		} else {
			return this.getToken(SparkSqlParser.PROPERTIES, i);
		}
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCreateNamespace) {
			listener.enterCreateNamespace(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCreateNamespace) {
			listener.exitCreateNamespace(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCreateNamespace) {
			return visitor.visitCreateNamespace(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SetNamespacePropertiesContext extends StatementContext {
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public namespace(): NamespaceContext {
		return this.getRuleContext(0, NamespaceContext);
	}
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public SET(): TerminalNode { return this.getToken(SparkSqlParser.SET, 0); }
	public tablePropertyList(): TablePropertyListContext {
		return this.getRuleContext(0, TablePropertyListContext);
	}
	public DBPROPERTIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DBPROPERTIES, 0); }
	public PROPERTIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PROPERTIES, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSetNamespaceProperties) {
			listener.enterSetNamespaceProperties(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSetNamespaceProperties) {
			listener.exitSetNamespaceProperties(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSetNamespaceProperties) {
			return visitor.visitSetNamespaceProperties(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SetNamespaceLocationContext extends StatementContext {
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public namespace(): NamespaceContext {
		return this.getRuleContext(0, NamespaceContext);
	}
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public SET(): TerminalNode { return this.getToken(SparkSqlParser.SET, 0); }
	public locationSpec(): LocationSpecContext {
		return this.getRuleContext(0, LocationSpecContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSetNamespaceLocation) {
			listener.enterSetNamespaceLocation(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSetNamespaceLocation) {
			listener.exitSetNamespaceLocation(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSetNamespaceLocation) {
			return visitor.visitSetNamespaceLocation(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DropNamespaceContext extends StatementContext {
	public DROP(): TerminalNode { return this.getToken(SparkSqlParser.DROP, 0); }
	public namespace(): NamespaceContext {
		return this.getRuleContext(0, NamespaceContext);
	}
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	public RESTRICT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RESTRICT, 0); }
	public CASCADE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CASCADE, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDropNamespace) {
			listener.enterDropNamespace(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDropNamespace) {
			listener.exitDropNamespace(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDropNamespace) {
			return visitor.visitDropNamespace(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ShowNamespacesContext extends StatementContext {
	public _pattern!: Token;
	public SHOW(): TerminalNode { return this.getToken(SparkSqlParser.SHOW, 0); }
	public DATABASES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DATABASES, 0); }
	public NAMESPACES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NAMESPACES, 0); }
	public multipartIdentifier(): MultipartIdentifierContext | undefined {
		return this.tryGetRuleContext(0, MultipartIdentifierContext);
	}
	public FROM(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FROM, 0); }
	public IN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IN, 0); }
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	public LIKE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LIKE, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterShowNamespaces) {
			listener.enterShowNamespaces(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitShowNamespaces) {
			listener.exitShowNamespaces(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitShowNamespaces) {
			return visitor.visitShowNamespaces(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class CreateTableContext extends StatementContext {
	public createTableHeader(): CreateTableHeaderContext {
		return this.getRuleContext(0, CreateTableHeaderContext);
	}
	public tableProvider(): TableProviderContext {
		return this.getRuleContext(0, TableProviderContext);
	}
	public createTableClauses(): CreateTableClausesContext {
		return this.getRuleContext(0, CreateTableClausesContext);
	}
	public colTypeList(): ColTypeListContext | undefined {
		return this.tryGetRuleContext(0, ColTypeListContext);
	}
	public query(): QueryContext | undefined {
		return this.tryGetRuleContext(0, QueryContext);
	}
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCreateTable) {
			listener.enterCreateTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCreateTable) {
			listener.exitCreateTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCreateTable) {
			return visitor.visitCreateTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class CreateHiveTableContext extends StatementContext {
	public _columns!: ColTypeListContext;
	public _partitionColumns!: ColTypeListContext;
	public _partitionColumnNames!: IdentifierListContext;
	public _tableProps!: TablePropertyListContext;
	public createTableHeader(): CreateTableHeaderContext {
		return this.getRuleContext(0, CreateTableHeaderContext);
	}
	public commentSpec(): CommentSpecContext[];
	public commentSpec(i: number): CommentSpecContext;
	public commentSpec(i?: number): CommentSpecContext | CommentSpecContext[] {
		if (i === undefined) {
			return this.getRuleContexts(CommentSpecContext);
		} else {
			return this.getRuleContext(i, CommentSpecContext);
		}
	}
	public bucketSpec(): BucketSpecContext[];
	public bucketSpec(i: number): BucketSpecContext;
	public bucketSpec(i?: number): BucketSpecContext | BucketSpecContext[] {
		if (i === undefined) {
			return this.getRuleContexts(BucketSpecContext);
		} else {
			return this.getRuleContext(i, BucketSpecContext);
		}
	}
	public skewSpec(): SkewSpecContext[];
	public skewSpec(i: number): SkewSpecContext;
	public skewSpec(i?: number): SkewSpecContext | SkewSpecContext[] {
		if (i === undefined) {
			return this.getRuleContexts(SkewSpecContext);
		} else {
			return this.getRuleContext(i, SkewSpecContext);
		}
	}
	public rowFormat(): RowFormatContext[];
	public rowFormat(i: number): RowFormatContext;
	public rowFormat(i?: number): RowFormatContext | RowFormatContext[] {
		if (i === undefined) {
			return this.getRuleContexts(RowFormatContext);
		} else {
			return this.getRuleContext(i, RowFormatContext);
		}
	}
	public createFileFormat(): CreateFileFormatContext[];
	public createFileFormat(i: number): CreateFileFormatContext;
	public createFileFormat(i?: number): CreateFileFormatContext | CreateFileFormatContext[] {
		if (i === undefined) {
			return this.getRuleContexts(CreateFileFormatContext);
		} else {
			return this.getRuleContext(i, CreateFileFormatContext);
		}
	}
	public locationSpec(): LocationSpecContext[];
	public locationSpec(i: number): LocationSpecContext;
	public locationSpec(i?: number): LocationSpecContext | LocationSpecContext[] {
		if (i === undefined) {
			return this.getRuleContexts(LocationSpecContext);
		} else {
			return this.getRuleContext(i, LocationSpecContext);
		}
	}
	public query(): QueryContext | undefined {
		return this.tryGetRuleContext(0, QueryContext);
	}
	public colTypeList(): ColTypeListContext[];
	public colTypeList(i: number): ColTypeListContext;
	public colTypeList(i?: number): ColTypeListContext | ColTypeListContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ColTypeListContext);
		} else {
			return this.getRuleContext(i, ColTypeListContext);
		}
	}
	public PARTITIONED(): TerminalNode[];
	public PARTITIONED(i: number): TerminalNode;
	public PARTITIONED(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.PARTITIONED);
		} else {
			return this.getToken(SparkSqlParser.PARTITIONED, i);
		}
	}
	public BY(): TerminalNode[];
	public BY(i: number): TerminalNode;
	public BY(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.BY);
		} else {
			return this.getToken(SparkSqlParser.BY, i);
		}
	}
	public TBLPROPERTIES(): TerminalNode[];
	public TBLPROPERTIES(i: number): TerminalNode;
	public TBLPROPERTIES(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.TBLPROPERTIES);
		} else {
			return this.getToken(SparkSqlParser.TBLPROPERTIES, i);
		}
	}
	public identifierList(): IdentifierListContext[];
	public identifierList(i: number): IdentifierListContext;
	public identifierList(i?: number): IdentifierListContext | IdentifierListContext[] {
		if (i === undefined) {
			return this.getRuleContexts(IdentifierListContext);
		} else {
			return this.getRuleContext(i, IdentifierListContext);
		}
	}
	public tablePropertyList(): TablePropertyListContext[];
	public tablePropertyList(i: number): TablePropertyListContext;
	public tablePropertyList(i?: number): TablePropertyListContext | TablePropertyListContext[] {
		if (i === undefined) {
			return this.getRuleContexts(TablePropertyListContext);
		} else {
			return this.getRuleContext(i, TablePropertyListContext);
		}
	}
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCreateHiveTable) {
			listener.enterCreateHiveTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCreateHiveTable) {
			listener.exitCreateHiveTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCreateHiveTable) {
			return visitor.visitCreateHiveTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class CreateTableLikeContext extends StatementContext {
	public _target!: TableIdentifierContext;
	public _source!: TableIdentifierContext;
	public _tableProps!: TablePropertyListContext;
	public CREATE(): TerminalNode { return this.getToken(SparkSqlParser.CREATE, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public LIKE(): TerminalNode { return this.getToken(SparkSqlParser.LIKE, 0); }
	public tableIdentifier(): TableIdentifierContext[];
	public tableIdentifier(i: number): TableIdentifierContext;
	public tableIdentifier(i?: number): TableIdentifierContext | TableIdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(TableIdentifierContext);
		} else {
			return this.getRuleContext(i, TableIdentifierContext);
		}
	}
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	public tableProvider(): TableProviderContext[];
	public tableProvider(i: number): TableProviderContext;
	public tableProvider(i?: number): TableProviderContext | TableProviderContext[] {
		if (i === undefined) {
			return this.getRuleContexts(TableProviderContext);
		} else {
			return this.getRuleContext(i, TableProviderContext);
		}
	}
	public rowFormat(): RowFormatContext[];
	public rowFormat(i: number): RowFormatContext;
	public rowFormat(i?: number): RowFormatContext | RowFormatContext[] {
		if (i === undefined) {
			return this.getRuleContexts(RowFormatContext);
		} else {
			return this.getRuleContext(i, RowFormatContext);
		}
	}
	public createFileFormat(): CreateFileFormatContext[];
	public createFileFormat(i: number): CreateFileFormatContext;
	public createFileFormat(i?: number): CreateFileFormatContext | CreateFileFormatContext[] {
		if (i === undefined) {
			return this.getRuleContexts(CreateFileFormatContext);
		} else {
			return this.getRuleContext(i, CreateFileFormatContext);
		}
	}
	public locationSpec(): LocationSpecContext[];
	public locationSpec(i: number): LocationSpecContext;
	public locationSpec(i?: number): LocationSpecContext | LocationSpecContext[] {
		if (i === undefined) {
			return this.getRuleContexts(LocationSpecContext);
		} else {
			return this.getRuleContext(i, LocationSpecContext);
		}
	}
	public TBLPROPERTIES(): TerminalNode[];
	public TBLPROPERTIES(i: number): TerminalNode;
	public TBLPROPERTIES(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.TBLPROPERTIES);
		} else {
			return this.getToken(SparkSqlParser.TBLPROPERTIES, i);
		}
	}
	public tablePropertyList(): TablePropertyListContext[];
	public tablePropertyList(i: number): TablePropertyListContext;
	public tablePropertyList(i?: number): TablePropertyListContext | TablePropertyListContext[] {
		if (i === undefined) {
			return this.getRuleContexts(TablePropertyListContext);
		} else {
			return this.getRuleContext(i, TablePropertyListContext);
		}
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCreateTableLike) {
			listener.enterCreateTableLike(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCreateTableLike) {
			listener.exitCreateTableLike(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCreateTableLike) {
			return visitor.visitCreateTableLike(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ReplaceTableContext extends StatementContext {
	public replaceTableHeader(): ReplaceTableHeaderContext {
		return this.getRuleContext(0, ReplaceTableHeaderContext);
	}
	public tableProvider(): TableProviderContext {
		return this.getRuleContext(0, TableProviderContext);
	}
	public createTableClauses(): CreateTableClausesContext {
		return this.getRuleContext(0, CreateTableClausesContext);
	}
	public colTypeList(): ColTypeListContext | undefined {
		return this.tryGetRuleContext(0, ColTypeListContext);
	}
	public query(): QueryContext | undefined {
		return this.tryGetRuleContext(0, QueryContext);
	}
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterReplaceTable) {
			listener.enterReplaceTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitReplaceTable) {
			listener.exitReplaceTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitReplaceTable) {
			return visitor.visitReplaceTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class AnalyzeContext extends StatementContext {
	public ANALYZE(): TerminalNode { return this.getToken(SparkSqlParser.ANALYZE, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public COMPUTE(): TerminalNode { return this.getToken(SparkSqlParser.COMPUTE, 0); }
	public STATISTICS(): TerminalNode { return this.getToken(SparkSqlParser.STATISTICS, 0); }
	public partitionSpec(): PartitionSpecContext | undefined {
		return this.tryGetRuleContext(0, PartitionSpecContext);
	}
	public identifier(): IdentifierContext | undefined {
		return this.tryGetRuleContext(0, IdentifierContext);
	}
	public FOR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FOR, 0); }
	public COLUMNS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLUMNS, 0); }
	public identifierSeq(): IdentifierSeqContext | undefined {
		return this.tryGetRuleContext(0, IdentifierSeqContext);
	}
	public ALL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ALL, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterAnalyze) {
			listener.enterAnalyze(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitAnalyze) {
			listener.exitAnalyze(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitAnalyze) {
			return visitor.visitAnalyze(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class AddTableColumnsContext extends StatementContext {
	public _columns!: QualifiedColTypeWithPositionListContext;
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public ADD(): TerminalNode { return this.getToken(SparkSqlParser.ADD, 0); }
	public COLUMN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLUMN, 0); }
	public COLUMNS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLUMNS, 0); }
	public qualifiedColTypeWithPositionList(): QualifiedColTypeWithPositionListContext {
		return this.getRuleContext(0, QualifiedColTypeWithPositionListContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterAddTableColumns) {
			listener.enterAddTableColumns(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitAddTableColumns) {
			listener.exitAddTableColumns(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitAddTableColumns) {
			return visitor.visitAddTableColumns(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class RenameTableColumnContext extends StatementContext {
	public _table!: MultipartIdentifierContext;
	public _from!: MultipartIdentifierContext;
	public _to!: ErrorCapturingIdentifierContext;
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public RENAME(): TerminalNode { return this.getToken(SparkSqlParser.RENAME, 0); }
	public COLUMN(): TerminalNode { return this.getToken(SparkSqlParser.COLUMN, 0); }
	public TO(): TerminalNode { return this.getToken(SparkSqlParser.TO, 0); }
	public multipartIdentifier(): MultipartIdentifierContext[];
	public multipartIdentifier(i: number): MultipartIdentifierContext;
	public multipartIdentifier(i?: number): MultipartIdentifierContext | MultipartIdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(MultipartIdentifierContext);
		} else {
			return this.getRuleContext(i, MultipartIdentifierContext);
		}
	}
	public errorCapturingIdentifier(): ErrorCapturingIdentifierContext {
		return this.getRuleContext(0, ErrorCapturingIdentifierContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterRenameTableColumn) {
			listener.enterRenameTableColumn(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitRenameTableColumn) {
			listener.exitRenameTableColumn(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitRenameTableColumn) {
			return visitor.visitRenameTableColumn(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DropTableColumnsContext extends StatementContext {
	public _columns!: MultipartIdentifierListContext;
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public DROP(): TerminalNode { return this.getToken(SparkSqlParser.DROP, 0); }
	public COLUMN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLUMN, 0); }
	public COLUMNS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLUMNS, 0); }
	public multipartIdentifierList(): MultipartIdentifierListContext {
		return this.getRuleContext(0, MultipartIdentifierListContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDropTableColumns) {
			listener.enterDropTableColumns(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDropTableColumns) {
			listener.exitDropTableColumns(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDropTableColumns) {
			return visitor.visitDropTableColumns(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class RenameTableContext extends StatementContext {
	public _from!: MultipartIdentifierContext;
	public _to!: MultipartIdentifierContext;
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public RENAME(): TerminalNode { return this.getToken(SparkSqlParser.RENAME, 0); }
	public TO(): TerminalNode { return this.getToken(SparkSqlParser.TO, 0); }
	public TABLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TABLE, 0); }
	public VIEW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.VIEW, 0); }
	public multipartIdentifier(): MultipartIdentifierContext[];
	public multipartIdentifier(i: number): MultipartIdentifierContext;
	public multipartIdentifier(i?: number): MultipartIdentifierContext | MultipartIdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(MultipartIdentifierContext);
		} else {
			return this.getRuleContext(i, MultipartIdentifierContext);
		}
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterRenameTable) {
			listener.enterRenameTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitRenameTable) {
			listener.exitRenameTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitRenameTable) {
			return visitor.visitRenameTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SetTablePropertiesContext extends StatementContext {
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public SET(): TerminalNode { return this.getToken(SparkSqlParser.SET, 0); }
	public TBLPROPERTIES(): TerminalNode { return this.getToken(SparkSqlParser.TBLPROPERTIES, 0); }
	public tablePropertyList(): TablePropertyListContext {
		return this.getRuleContext(0, TablePropertyListContext);
	}
	public TABLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TABLE, 0); }
	public VIEW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.VIEW, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSetTableProperties) {
			listener.enterSetTableProperties(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSetTableProperties) {
			listener.exitSetTableProperties(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSetTableProperties) {
			return visitor.visitSetTableProperties(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class UnsetTablePropertiesContext extends StatementContext {
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public UNSET(): TerminalNode { return this.getToken(SparkSqlParser.UNSET, 0); }
	public TBLPROPERTIES(): TerminalNode { return this.getToken(SparkSqlParser.TBLPROPERTIES, 0); }
	public tablePropertyList(): TablePropertyListContext {
		return this.getRuleContext(0, TablePropertyListContext);
	}
	public TABLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TABLE, 0); }
	public VIEW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.VIEW, 0); }
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterUnsetTableProperties) {
			listener.enterUnsetTableProperties(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitUnsetTableProperties) {
			listener.exitUnsetTableProperties(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitUnsetTableProperties) {
			return visitor.visitUnsetTableProperties(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class AlterTableAlterColumnContext extends StatementContext {
	public _table!: MultipartIdentifierContext;
	public _column!: MultipartIdentifierContext;
	public ALTER(): TerminalNode[];
	public ALTER(i: number): TerminalNode;
	public ALTER(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.ALTER);
		} else {
			return this.getToken(SparkSqlParser.ALTER, i);
		}
	}
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext[];
	public multipartIdentifier(i: number): MultipartIdentifierContext;
	public multipartIdentifier(i?: number): MultipartIdentifierContext | MultipartIdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(MultipartIdentifierContext);
		} else {
			return this.getRuleContext(i, MultipartIdentifierContext);
		}
	}
	public CHANGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CHANGE, 0); }
	public COLUMN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLUMN, 0); }
	public alterColumnAction(): AlterColumnActionContext | undefined {
		return this.tryGetRuleContext(0, AlterColumnActionContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterAlterTableAlterColumn) {
			listener.enterAlterTableAlterColumn(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitAlterTableAlterColumn) {
			listener.exitAlterTableAlterColumn(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitAlterTableAlterColumn) {
			return visitor.visitAlterTableAlterColumn(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class HiveChangeColumnContext extends StatementContext {
	public _table!: MultipartIdentifierContext;
	public _colName!: MultipartIdentifierContext;
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public CHANGE(): TerminalNode { return this.getToken(SparkSqlParser.CHANGE, 0); }
	public colType(): ColTypeContext {
		return this.getRuleContext(0, ColTypeContext);
	}
	public multipartIdentifier(): MultipartIdentifierContext[];
	public multipartIdentifier(i: number): MultipartIdentifierContext;
	public multipartIdentifier(i?: number): MultipartIdentifierContext | MultipartIdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(MultipartIdentifierContext);
		} else {
			return this.getRuleContext(i, MultipartIdentifierContext);
		}
	}
	public partitionSpec(): PartitionSpecContext | undefined {
		return this.tryGetRuleContext(0, PartitionSpecContext);
	}
	public COLUMN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLUMN, 0); }
	public colPosition(): ColPositionContext | undefined {
		return this.tryGetRuleContext(0, ColPositionContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterHiveChangeColumn) {
			listener.enterHiveChangeColumn(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitHiveChangeColumn) {
			listener.exitHiveChangeColumn(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitHiveChangeColumn) {
			return visitor.visitHiveChangeColumn(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class HiveReplaceColumnsContext extends StatementContext {
	public _table!: MultipartIdentifierContext;
	public _columns!: QualifiedColTypeWithPositionListContext;
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public REPLACE(): TerminalNode { return this.getToken(SparkSqlParser.REPLACE, 0); }
	public COLUMNS(): TerminalNode { return this.getToken(SparkSqlParser.COLUMNS, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public qualifiedColTypeWithPositionList(): QualifiedColTypeWithPositionListContext {
		return this.getRuleContext(0, QualifiedColTypeWithPositionListContext);
	}
	public partitionSpec(): PartitionSpecContext | undefined {
		return this.tryGetRuleContext(0, PartitionSpecContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterHiveReplaceColumns) {
			listener.enterHiveReplaceColumns(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitHiveReplaceColumns) {
			listener.exitHiveReplaceColumns(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitHiveReplaceColumns) {
			return visitor.visitHiveReplaceColumns(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SetTableSerDeContext extends StatementContext {
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public SET(): TerminalNode { return this.getToken(SparkSqlParser.SET, 0); }
	public SERDE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SERDE, 0); }
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	public partitionSpec(): PartitionSpecContext | undefined {
		return this.tryGetRuleContext(0, PartitionSpecContext);
	}
	public WITH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.WITH, 0); }
	public SERDEPROPERTIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SERDEPROPERTIES, 0); }
	public tablePropertyList(): TablePropertyListContext | undefined {
		return this.tryGetRuleContext(0, TablePropertyListContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSetTableSerDe) {
			listener.enterSetTableSerDe(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSetTableSerDe) {
			listener.exitSetTableSerDe(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSetTableSerDe) {
			return visitor.visitSetTableSerDe(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class AddTablePartitionContext extends StatementContext {
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public ADD(): TerminalNode { return this.getToken(SparkSqlParser.ADD, 0); }
	public TABLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TABLE, 0); }
	public VIEW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.VIEW, 0); }
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	public partitionSpecLocation(): PartitionSpecLocationContext[];
	public partitionSpecLocation(i: number): PartitionSpecLocationContext;
	public partitionSpecLocation(i?: number): PartitionSpecLocationContext | PartitionSpecLocationContext[] {
		if (i === undefined) {
			return this.getRuleContexts(PartitionSpecLocationContext);
		} else {
			return this.getRuleContext(i, PartitionSpecLocationContext);
		}
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterAddTablePartition) {
			listener.enterAddTablePartition(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitAddTablePartition) {
			listener.exitAddTablePartition(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitAddTablePartition) {
			return visitor.visitAddTablePartition(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class RenameTablePartitionContext extends StatementContext {
	public _from!: PartitionSpecContext;
	public _to!: PartitionSpecContext;
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public RENAME(): TerminalNode { return this.getToken(SparkSqlParser.RENAME, 0); }
	public TO(): TerminalNode { return this.getToken(SparkSqlParser.TO, 0); }
	public partitionSpec(): PartitionSpecContext[];
	public partitionSpec(i: number): PartitionSpecContext;
	public partitionSpec(i?: number): PartitionSpecContext | PartitionSpecContext[] {
		if (i === undefined) {
			return this.getRuleContexts(PartitionSpecContext);
		} else {
			return this.getRuleContext(i, PartitionSpecContext);
		}
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterRenameTablePartition) {
			listener.enterRenameTablePartition(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitRenameTablePartition) {
			listener.exitRenameTablePartition(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitRenameTablePartition) {
			return visitor.visitRenameTablePartition(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DropTablePartitionsContext extends StatementContext {
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public DROP(): TerminalNode { return this.getToken(SparkSqlParser.DROP, 0); }
	public partitionSpec(): PartitionSpecContext[];
	public partitionSpec(i: number): PartitionSpecContext;
	public partitionSpec(i?: number): PartitionSpecContext | PartitionSpecContext[] {
		if (i === undefined) {
			return this.getRuleContexts(PartitionSpecContext);
		} else {
			return this.getRuleContext(i, PartitionSpecContext);
		}
	}
	public TABLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TABLE, 0); }
	public VIEW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.VIEW, 0); }
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	public PURGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PURGE, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDropTablePartitions) {
			listener.enterDropTablePartitions(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDropTablePartitions) {
			listener.exitDropTablePartitions(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDropTablePartitions) {
			return visitor.visitDropTablePartitions(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SetTableLocationContext extends StatementContext {
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public SET(): TerminalNode { return this.getToken(SparkSqlParser.SET, 0); }
	public locationSpec(): LocationSpecContext {
		return this.getRuleContext(0, LocationSpecContext);
	}
	public partitionSpec(): PartitionSpecContext | undefined {
		return this.tryGetRuleContext(0, PartitionSpecContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSetTableLocation) {
			listener.enterSetTableLocation(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSetTableLocation) {
			listener.exitSetTableLocation(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSetTableLocation) {
			return visitor.visitSetTableLocation(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class RecoverPartitionsContext extends StatementContext {
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public RECOVER(): TerminalNode { return this.getToken(SparkSqlParser.RECOVER, 0); }
	public PARTITIONS(): TerminalNode { return this.getToken(SparkSqlParser.PARTITIONS, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterRecoverPartitions) {
			listener.enterRecoverPartitions(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitRecoverPartitions) {
			listener.exitRecoverPartitions(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitRecoverPartitions) {
			return visitor.visitRecoverPartitions(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DropTableContext extends StatementContext {
	public DROP(): TerminalNode { return this.getToken(SparkSqlParser.DROP, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	public PURGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PURGE, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDropTable) {
			listener.enterDropTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDropTable) {
			listener.exitDropTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDropTable) {
			return visitor.visitDropTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DropViewContext extends StatementContext {
	public DROP(): TerminalNode { return this.getToken(SparkSqlParser.DROP, 0); }
	public VIEW(): TerminalNode { return this.getToken(SparkSqlParser.VIEW, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDropView) {
			listener.enterDropView(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDropView) {
			listener.exitDropView(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDropView) {
			return visitor.visitDropView(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class CreateViewContext extends StatementContext {
	public CREATE(): TerminalNode { return this.getToken(SparkSqlParser.CREATE, 0); }
	public VIEW(): TerminalNode { return this.getToken(SparkSqlParser.VIEW, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public AS(): TerminalNode { return this.getToken(SparkSqlParser.AS, 0); }
	public query(): QueryContext {
		return this.getRuleContext(0, QueryContext);
	}
	public OR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OR, 0); }
	public REPLACE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REPLACE, 0); }
	public TEMPORARY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TEMPORARY, 0); }
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	public identifierCommentList(): IdentifierCommentListContext | undefined {
		return this.tryGetRuleContext(0, IdentifierCommentListContext);
	}
	public commentSpec(): CommentSpecContext[];
	public commentSpec(i: number): CommentSpecContext;
	public commentSpec(i?: number): CommentSpecContext | CommentSpecContext[] {
		if (i === undefined) {
			return this.getRuleContexts(CommentSpecContext);
		} else {
			return this.getRuleContext(i, CommentSpecContext);
		}
	}
	public PARTITIONED(): TerminalNode[];
	public PARTITIONED(i: number): TerminalNode;
	public PARTITIONED(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.PARTITIONED);
		} else {
			return this.getToken(SparkSqlParser.PARTITIONED, i);
		}
	}
	public ON(): TerminalNode[];
	public ON(i: number): TerminalNode;
	public ON(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.ON);
		} else {
			return this.getToken(SparkSqlParser.ON, i);
		}
	}
	public identifierList(): IdentifierListContext[];
	public identifierList(i: number): IdentifierListContext;
	public identifierList(i?: number): IdentifierListContext | IdentifierListContext[] {
		if (i === undefined) {
			return this.getRuleContexts(IdentifierListContext);
		} else {
			return this.getRuleContext(i, IdentifierListContext);
		}
	}
	public TBLPROPERTIES(): TerminalNode[];
	public TBLPROPERTIES(i: number): TerminalNode;
	public TBLPROPERTIES(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.TBLPROPERTIES);
		} else {
			return this.getToken(SparkSqlParser.TBLPROPERTIES, i);
		}
	}
	public tablePropertyList(): TablePropertyListContext[];
	public tablePropertyList(i: number): TablePropertyListContext;
	public tablePropertyList(i?: number): TablePropertyListContext | TablePropertyListContext[] {
		if (i === undefined) {
			return this.getRuleContexts(TablePropertyListContext);
		} else {
			return this.getRuleContext(i, TablePropertyListContext);
		}
	}
	public GLOBAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.GLOBAL, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCreateView) {
			listener.enterCreateView(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCreateView) {
			listener.exitCreateView(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCreateView) {
			return visitor.visitCreateView(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class CreateTempViewUsingContext extends StatementContext {
	public CREATE(): TerminalNode { return this.getToken(SparkSqlParser.CREATE, 0); }
	public TEMPORARY(): TerminalNode { return this.getToken(SparkSqlParser.TEMPORARY, 0); }
	public VIEW(): TerminalNode { return this.getToken(SparkSqlParser.VIEW, 0); }
	public tableIdentifier(): TableIdentifierContext {
		return this.getRuleContext(0, TableIdentifierContext);
	}
	public tableProvider(): TableProviderContext {
		return this.getRuleContext(0, TableProviderContext);
	}
	public OR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OR, 0); }
	public REPLACE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REPLACE, 0); }
	public GLOBAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.GLOBAL, 0); }
	public colTypeList(): ColTypeListContext | undefined {
		return this.tryGetRuleContext(0, ColTypeListContext);
	}
	public OPTIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OPTIONS, 0); }
	public tablePropertyList(): TablePropertyListContext | undefined {
		return this.tryGetRuleContext(0, TablePropertyListContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCreateTempViewUsing) {
			listener.enterCreateTempViewUsing(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCreateTempViewUsing) {
			listener.exitCreateTempViewUsing(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCreateTempViewUsing) {
			return visitor.visitCreateTempViewUsing(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class AlterViewQueryContext extends StatementContext {
	public ALTER(): TerminalNode { return this.getToken(SparkSqlParser.ALTER, 0); }
	public VIEW(): TerminalNode { return this.getToken(SparkSqlParser.VIEW, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public query(): QueryContext {
		return this.getRuleContext(0, QueryContext);
	}
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterAlterViewQuery) {
			listener.enterAlterViewQuery(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitAlterViewQuery) {
			listener.exitAlterViewQuery(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitAlterViewQuery) {
			return visitor.visitAlterViewQuery(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class CreateFunctionContext extends StatementContext {
	public _className!: Token;
	public CREATE(): TerminalNode { return this.getToken(SparkSqlParser.CREATE, 0); }
	public FUNCTION(): TerminalNode { return this.getToken(SparkSqlParser.FUNCTION, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public AS(): TerminalNode { return this.getToken(SparkSqlParser.AS, 0); }
	public STRING(): TerminalNode { return this.getToken(SparkSqlParser.STRING, 0); }
	public OR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OR, 0); }
	public REPLACE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REPLACE, 0); }
	public TEMPORARY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TEMPORARY, 0); }
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	public USING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.USING, 0); }
	public resource(): ResourceContext[];
	public resource(i: number): ResourceContext;
	public resource(i?: number): ResourceContext | ResourceContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ResourceContext);
		} else {
			return this.getRuleContext(i, ResourceContext);
		}
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCreateFunction) {
			listener.enterCreateFunction(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCreateFunction) {
			listener.exitCreateFunction(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCreateFunction) {
			return visitor.visitCreateFunction(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DropFunctionContext extends StatementContext {
	public DROP(): TerminalNode { return this.getToken(SparkSqlParser.DROP, 0); }
	public FUNCTION(): TerminalNode { return this.getToken(SparkSqlParser.FUNCTION, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public TEMPORARY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TEMPORARY, 0); }
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDropFunction) {
			listener.enterDropFunction(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDropFunction) {
			listener.exitDropFunction(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDropFunction) {
			return visitor.visitDropFunction(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ExplainContext extends StatementContext {
	public EXPLAIN(): TerminalNode { return this.getToken(SparkSqlParser.EXPLAIN, 0); }
	public statement(): StatementContext {
		return this.getRuleContext(0, StatementContext);
	}
	public LOGICAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOGICAL, 0); }
	public FORMATTED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FORMATTED, 0); }
	public EXTENDED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXTENDED, 0); }
	public CODEGEN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CODEGEN, 0); }
	public COST(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COST, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterExplain) {
			listener.enterExplain(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitExplain) {
			listener.exitExplain(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitExplain) {
			return visitor.visitExplain(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ShowTablesContext extends StatementContext {
	public _pattern!: Token;
	public SHOW(): TerminalNode { return this.getToken(SparkSqlParser.SHOW, 0); }
	public TABLES(): TerminalNode { return this.getToken(SparkSqlParser.TABLES, 0); }
	public multipartIdentifier(): MultipartIdentifierContext | undefined {
		return this.tryGetRuleContext(0, MultipartIdentifierContext);
	}
	public FROM(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FROM, 0); }
	public IN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IN, 0); }
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	public LIKE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LIKE, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterShowTables) {
			listener.enterShowTables(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitShowTables) {
			listener.exitShowTables(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitShowTables) {
			return visitor.visitShowTables(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ShowTableContext extends StatementContext {
	public _ns!: MultipartIdentifierContext;
	public _pattern!: Token;
	public SHOW(): TerminalNode { return this.getToken(SparkSqlParser.SHOW, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public EXTENDED(): TerminalNode { return this.getToken(SparkSqlParser.EXTENDED, 0); }
	public LIKE(): TerminalNode { return this.getToken(SparkSqlParser.LIKE, 0); }
	public STRING(): TerminalNode { return this.getToken(SparkSqlParser.STRING, 0); }
	public partitionSpec(): PartitionSpecContext | undefined {
		return this.tryGetRuleContext(0, PartitionSpecContext);
	}
	public FROM(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FROM, 0); }
	public IN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IN, 0); }
	public multipartIdentifier(): MultipartIdentifierContext | undefined {
		return this.tryGetRuleContext(0, MultipartIdentifierContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterShowTable) {
			listener.enterShowTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitShowTable) {
			listener.exitShowTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitShowTable) {
			return visitor.visitShowTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ShowTblPropertiesContext extends StatementContext {
	public _table!: MultipartIdentifierContext;
	public _key!: TablePropertyKeyContext;
	public SHOW(): TerminalNode { return this.getToken(SparkSqlParser.SHOW, 0); }
	public TBLPROPERTIES(): TerminalNode { return this.getToken(SparkSqlParser.TBLPROPERTIES, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public tablePropertyKey(): TablePropertyKeyContext | undefined {
		return this.tryGetRuleContext(0, TablePropertyKeyContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterShowTblProperties) {
			listener.enterShowTblProperties(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitShowTblProperties) {
			listener.exitShowTblProperties(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitShowTblProperties) {
			return visitor.visitShowTblProperties(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ShowColumnsContext extends StatementContext {
	public _table!: MultipartIdentifierContext;
	public _ns!: MultipartIdentifierContext;
	public SHOW(): TerminalNode { return this.getToken(SparkSqlParser.SHOW, 0); }
	public COLUMNS(): TerminalNode { return this.getToken(SparkSqlParser.COLUMNS, 0); }
	public FROM(): TerminalNode[];
	public FROM(i: number): TerminalNode;
	public FROM(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.FROM);
		} else {
			return this.getToken(SparkSqlParser.FROM, i);
		}
	}
	public IN(): TerminalNode[];
	public IN(i: number): TerminalNode;
	public IN(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.IN);
		} else {
			return this.getToken(SparkSqlParser.IN, i);
		}
	}
	public multipartIdentifier(): MultipartIdentifierContext[];
	public multipartIdentifier(i: number): MultipartIdentifierContext;
	public multipartIdentifier(i?: number): MultipartIdentifierContext | MultipartIdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(MultipartIdentifierContext);
		} else {
			return this.getRuleContext(i, MultipartIdentifierContext);
		}
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterShowColumns) {
			listener.enterShowColumns(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitShowColumns) {
			listener.exitShowColumns(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitShowColumns) {
			return visitor.visitShowColumns(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ShowViewsContext extends StatementContext {
	public _pattern!: Token;
	public SHOW(): TerminalNode { return this.getToken(SparkSqlParser.SHOW, 0); }
	public VIEWS(): TerminalNode { return this.getToken(SparkSqlParser.VIEWS, 0); }
	public multipartIdentifier(): MultipartIdentifierContext | undefined {
		return this.tryGetRuleContext(0, MultipartIdentifierContext);
	}
	public FROM(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FROM, 0); }
	public IN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IN, 0); }
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	public LIKE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LIKE, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterShowViews) {
			listener.enterShowViews(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitShowViews) {
			listener.exitShowViews(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitShowViews) {
			return visitor.visitShowViews(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ShowPartitionsContext extends StatementContext {
	public SHOW(): TerminalNode { return this.getToken(SparkSqlParser.SHOW, 0); }
	public PARTITIONS(): TerminalNode { return this.getToken(SparkSqlParser.PARTITIONS, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public partitionSpec(): PartitionSpecContext | undefined {
		return this.tryGetRuleContext(0, PartitionSpecContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterShowPartitions) {
			listener.enterShowPartitions(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitShowPartitions) {
			listener.exitShowPartitions(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitShowPartitions) {
			return visitor.visitShowPartitions(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ShowFunctionsContext extends StatementContext {
	public _pattern!: Token;
	public SHOW(): TerminalNode { return this.getToken(SparkSqlParser.SHOW, 0); }
	public FUNCTIONS(): TerminalNode { return this.getToken(SparkSqlParser.FUNCTIONS, 0); }
	public identifier(): IdentifierContext | undefined {
		return this.tryGetRuleContext(0, IdentifierContext);
	}
	public multipartIdentifier(): MultipartIdentifierContext | undefined {
		return this.tryGetRuleContext(0, MultipartIdentifierContext);
	}
	public LIKE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LIKE, 0); }
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterShowFunctions) {
			listener.enterShowFunctions(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitShowFunctions) {
			listener.exitShowFunctions(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitShowFunctions) {
			return visitor.visitShowFunctions(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ShowCreateTableContext extends StatementContext {
	public SHOW(): TerminalNode { return this.getToken(SparkSqlParser.SHOW, 0); }
	public CREATE(): TerminalNode { return this.getToken(SparkSqlParser.CREATE, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	public SERDE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SERDE, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterShowCreateTable) {
			listener.enterShowCreateTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitShowCreateTable) {
			listener.exitShowCreateTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitShowCreateTable) {
			return visitor.visitShowCreateTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ShowCurrentNamespaceContext extends StatementContext {
	public SHOW(): TerminalNode { return this.getToken(SparkSqlParser.SHOW, 0); }
	public CURRENT(): TerminalNode { return this.getToken(SparkSqlParser.CURRENT, 0); }
	public NAMESPACE(): TerminalNode { return this.getToken(SparkSqlParser.NAMESPACE, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterShowCurrentNamespace) {
			listener.enterShowCurrentNamespace(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitShowCurrentNamespace) {
			listener.exitShowCurrentNamespace(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitShowCurrentNamespace) {
			return visitor.visitShowCurrentNamespace(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DescribeFunctionContext extends StatementContext {
	public FUNCTION(): TerminalNode { return this.getToken(SparkSqlParser.FUNCTION, 0); }
	public describeFuncName(): DescribeFuncNameContext {
		return this.getRuleContext(0, DescribeFuncNameContext);
	}
	public DESC(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DESC, 0); }
	public DESCRIBE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DESCRIBE, 0); }
	public EXTENDED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXTENDED, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDescribeFunction) {
			listener.enterDescribeFunction(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDescribeFunction) {
			listener.exitDescribeFunction(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDescribeFunction) {
			return visitor.visitDescribeFunction(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DescribeNamespaceContext extends StatementContext {
	public namespace(): NamespaceContext {
		return this.getRuleContext(0, NamespaceContext);
	}
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public DESC(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DESC, 0); }
	public DESCRIBE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DESCRIBE, 0); }
	public EXTENDED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXTENDED, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDescribeNamespace) {
			listener.enterDescribeNamespace(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDescribeNamespace) {
			listener.exitDescribeNamespace(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDescribeNamespace) {
			return visitor.visitDescribeNamespace(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DescribeRelationContext extends StatementContext {
	public _option!: Token;
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public DESC(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DESC, 0); }
	public DESCRIBE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DESCRIBE, 0); }
	public TABLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TABLE, 0); }
	public partitionSpec(): PartitionSpecContext | undefined {
		return this.tryGetRuleContext(0, PartitionSpecContext);
	}
	public describeColName(): DescribeColNameContext | undefined {
		return this.tryGetRuleContext(0, DescribeColNameContext);
	}
	public EXTENDED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXTENDED, 0); }
	public FORMATTED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FORMATTED, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDescribeRelation) {
			listener.enterDescribeRelation(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDescribeRelation) {
			listener.exitDescribeRelation(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDescribeRelation) {
			return visitor.visitDescribeRelation(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DescribeQueryContext extends StatementContext {
	public query(): QueryContext {
		return this.getRuleContext(0, QueryContext);
	}
	public DESC(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DESC, 0); }
	public DESCRIBE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DESCRIBE, 0); }
	public QUERY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.QUERY, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDescribeQuery) {
			listener.enterDescribeQuery(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDescribeQuery) {
			listener.exitDescribeQuery(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDescribeQuery) {
			return visitor.visitDescribeQuery(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class CommentNamespaceContext extends StatementContext {
	public _comment!: Token;
	public COMMENT(): TerminalNode { return this.getToken(SparkSqlParser.COMMENT, 0); }
	public ON(): TerminalNode { return this.getToken(SparkSqlParser.ON, 0); }
	public namespace(): NamespaceContext {
		return this.getRuleContext(0, NamespaceContext);
	}
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public IS(): TerminalNode { return this.getToken(SparkSqlParser.IS, 0); }
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	public NULL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NULL, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCommentNamespace) {
			listener.enterCommentNamespace(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCommentNamespace) {
			listener.exitCommentNamespace(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCommentNamespace) {
			return visitor.visitCommentNamespace(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class CommentTableContext extends StatementContext {
	public _comment!: Token;
	public COMMENT(): TerminalNode { return this.getToken(SparkSqlParser.COMMENT, 0); }
	public ON(): TerminalNode { return this.getToken(SparkSqlParser.ON, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public IS(): TerminalNode { return this.getToken(SparkSqlParser.IS, 0); }
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	public NULL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NULL, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCommentTable) {
			listener.enterCommentTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCommentTable) {
			listener.exitCommentTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCommentTable) {
			return visitor.visitCommentTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class RefreshTableContext extends StatementContext {
	public REFRESH(): TerminalNode { return this.getToken(SparkSqlParser.REFRESH, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterRefreshTable) {
			listener.enterRefreshTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitRefreshTable) {
			listener.exitRefreshTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitRefreshTable) {
			return visitor.visitRefreshTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class RefreshFunctionContext extends StatementContext {
	public REFRESH(): TerminalNode { return this.getToken(SparkSqlParser.REFRESH, 0); }
	public FUNCTION(): TerminalNode { return this.getToken(SparkSqlParser.FUNCTION, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterRefreshFunction) {
			listener.enterRefreshFunction(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitRefreshFunction) {
			listener.exitRefreshFunction(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitRefreshFunction) {
			return visitor.visitRefreshFunction(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class RefreshResourceContext extends StatementContext {
	public REFRESH(): TerminalNode { return this.getToken(SparkSqlParser.REFRESH, 0); }
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterRefreshResource) {
			listener.enterRefreshResource(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitRefreshResource) {
			listener.exitRefreshResource(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitRefreshResource) {
			return visitor.visitRefreshResource(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class CacheTableContext extends StatementContext {
	public CACHE(): TerminalNode { return this.getToken(SparkSqlParser.CACHE, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public LAZY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LAZY, 0); }
	public OPTIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OPTIONS, 0); }
	public tablePropertyList(): TablePropertyListContext | undefined {
		return this.tryGetRuleContext(0, TablePropertyListContext);
	}
	public query(): QueryContext | undefined {
		return this.tryGetRuleContext(0, QueryContext);
	}
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCacheTable) {
			listener.enterCacheTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCacheTable) {
			listener.exitCacheTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCacheTable) {
			return visitor.visitCacheTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class UncacheTableContext extends StatementContext {
	public UNCACHE(): TerminalNode { return this.getToken(SparkSqlParser.UNCACHE, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterUncacheTable) {
			listener.enterUncacheTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitUncacheTable) {
			listener.exitUncacheTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitUncacheTable) {
			return visitor.visitUncacheTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ClearCacheContext extends StatementContext {
	public CLEAR(): TerminalNode { return this.getToken(SparkSqlParser.CLEAR, 0); }
	public CACHE(): TerminalNode { return this.getToken(SparkSqlParser.CACHE, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterClearCache) {
			listener.enterClearCache(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitClearCache) {
			listener.exitClearCache(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitClearCache) {
			return visitor.visitClearCache(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class LoadDataContext extends StatementContext {
	public _path!: Token;
	public LOAD(): TerminalNode { return this.getToken(SparkSqlParser.LOAD, 0); }
	public DATA(): TerminalNode { return this.getToken(SparkSqlParser.DATA, 0); }
	public INPATH(): TerminalNode { return this.getToken(SparkSqlParser.INPATH, 0); }
	public INTO(): TerminalNode { return this.getToken(SparkSqlParser.INTO, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public STRING(): TerminalNode { return this.getToken(SparkSqlParser.STRING, 0); }
	public LOCAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCAL, 0); }
	public OVERWRITE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OVERWRITE, 0); }
	public partitionSpec(): PartitionSpecContext | undefined {
		return this.tryGetRuleContext(0, PartitionSpecContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterLoadData) {
			listener.enterLoadData(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitLoadData) {
			listener.exitLoadData(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitLoadData) {
			return visitor.visitLoadData(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class TruncateTableContext extends StatementContext {
	public TRUNCATE(): TerminalNode { return this.getToken(SparkSqlParser.TRUNCATE, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public partitionSpec(): PartitionSpecContext | undefined {
		return this.tryGetRuleContext(0, PartitionSpecContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTruncateTable) {
			listener.enterTruncateTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTruncateTable) {
			listener.exitTruncateTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTruncateTable) {
			return visitor.visitTruncateTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class RepairTableContext extends StatementContext {
	public MSCK(): TerminalNode { return this.getToken(SparkSqlParser.MSCK, 0); }
	public REPAIR(): TerminalNode { return this.getToken(SparkSqlParser.REPAIR, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterRepairTable) {
			listener.enterRepairTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitRepairTable) {
			listener.exitRepairTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitRepairTable) {
			return visitor.visitRepairTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ManageResourceContext extends StatementContext {
	public _op!: Token;
	public identifier(): IdentifierContext {
		return this.getRuleContext(0, IdentifierContext);
	}
	public ADD(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ADD, 0); }
	public LIST(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LIST, 0); }
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterManageResource) {
			listener.enterManageResource(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitManageResource) {
			listener.exitManageResource(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitManageResource) {
			return visitor.visitManageResource(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class FailNativeCommandContext extends StatementContext {
	public SET(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SET, 0); }
	public ROLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROLE, 0); }
	public unsupportedHiveNativeCommands(): UnsupportedHiveNativeCommandsContext | undefined {
		return this.tryGetRuleContext(0, UnsupportedHiveNativeCommandsContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterFailNativeCommand) {
			listener.enterFailNativeCommand(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitFailNativeCommand) {
			listener.exitFailNativeCommand(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitFailNativeCommand) {
			return visitor.visitFailNativeCommand(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SetTimeZoneContext extends StatementContext {
	public _timezone!: Token;
	public SET(): TerminalNode { return this.getToken(SparkSqlParser.SET, 0); }
	public TIME(): TerminalNode { return this.getToken(SparkSqlParser.TIME, 0); }
	public ZONE(): TerminalNode { return this.getToken(SparkSqlParser.ZONE, 0); }
	public interval(): IntervalContext | undefined {
		return this.tryGetRuleContext(0, IntervalContext);
	}
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	public LOCAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCAL, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSetTimeZone) {
			listener.enterSetTimeZone(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSetTimeZone) {
			listener.exitSetTimeZone(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSetTimeZone) {
			return visitor.visitSetTimeZone(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SetQuotedConfigurationContext extends StatementContext {
	public SET(): TerminalNode { return this.getToken(SparkSqlParser.SET, 0); }
	public configKey(): ConfigKeyContext {
		return this.getRuleContext(0, ConfigKeyContext);
	}
	public EQ(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EQ, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSetQuotedConfiguration) {
			listener.enterSetQuotedConfiguration(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSetQuotedConfiguration) {
			listener.exitSetQuotedConfiguration(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSetQuotedConfiguration) {
			return visitor.visitSetQuotedConfiguration(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SetConfigurationContext extends StatementContext {
	public SET(): TerminalNode { return this.getToken(SparkSqlParser.SET, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSetConfiguration) {
			listener.enterSetConfiguration(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSetConfiguration) {
			listener.exitSetConfiguration(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSetConfiguration) {
			return visitor.visitSetConfiguration(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ResetQuotedConfigurationContext extends StatementContext {
	public RESET(): TerminalNode { return this.getToken(SparkSqlParser.RESET, 0); }
	public configKey(): ConfigKeyContext {
		return this.getRuleContext(0, ConfigKeyContext);
	}
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterResetQuotedConfiguration) {
			listener.enterResetQuotedConfiguration(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitResetQuotedConfiguration) {
			listener.exitResetQuotedConfiguration(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitResetQuotedConfiguration) {
			return visitor.visitResetQuotedConfiguration(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ResetConfigurationContext extends StatementContext {
	public RESET(): TerminalNode { return this.getToken(SparkSqlParser.RESET, 0); }
	constructor(ctx: StatementContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterResetConfiguration) {
			listener.enterResetConfiguration(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitResetConfiguration) {
			listener.exitResetConfiguration(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitResetConfiguration) {
			return visitor.visitResetConfiguration(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ConfigKeyContext extends ParserRuleContext {
	public quotedIdentifier(): QuotedIdentifierContext {
		return this.getRuleContext(0, QuotedIdentifierContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_configKey; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterConfigKey) {
			listener.enterConfigKey(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitConfigKey) {
			listener.exitConfigKey(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitConfigKey) {
			return visitor.visitConfigKey(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class UnsupportedHiveNativeCommandsContext extends ParserRuleContext {
	public _kw1!: Token;
	public _kw2!: Token;
	public _kw3!: Token;
	public _kw4!: Token;
	public _kw5!: Token;
	public _kw6!: Token;
	public CREATE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CREATE, 0); }
	public ROLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROLE, 0); }
	public DROP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DROP, 0); }
	public GRANT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.GRANT, 0); }
	public REVOKE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REVOKE, 0); }
	public SHOW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SHOW, 0); }
	public PRINCIPALS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PRINCIPALS, 0); }
	public ROLES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROLES, 0); }
	public CURRENT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CURRENT, 0); }
	public EXPORT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXPORT, 0); }
	public TABLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TABLE, 0); }
	public IMPORT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IMPORT, 0); }
	public COMPACTIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COMPACTIONS, 0); }
	public TRANSACTIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRANSACTIONS, 0); }
	public INDEXES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INDEXES, 0); }
	public LOCKS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCKS, 0); }
	public INDEX(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INDEX, 0); }
	public ALTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ALTER, 0); }
	public LOCK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCK, 0); }
	public DATABASE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DATABASE, 0); }
	public UNLOCK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNLOCK, 0); }
	public TEMPORARY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TEMPORARY, 0); }
	public MACRO(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MACRO, 0); }
	public tableIdentifier(): TableIdentifierContext | undefined {
		return this.tryGetRuleContext(0, TableIdentifierContext);
	}
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public CLUSTERED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CLUSTERED, 0); }
	public BY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.BY, 0); }
	public SORTED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SORTED, 0); }
	public SKEWED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SKEWED, 0); }
	public STORED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STORED, 0); }
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	public DIRECTORIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DIRECTORIES, 0); }
	public SET(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SET, 0); }
	public LOCATION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCATION, 0); }
	public EXCHANGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXCHANGE, 0); }
	public PARTITION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PARTITION, 0); }
	public ARCHIVE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ARCHIVE, 0); }
	public UNARCHIVE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNARCHIVE, 0); }
	public TOUCH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TOUCH, 0); }
	public COMPACT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COMPACT, 0); }
	public partitionSpec(): PartitionSpecContext | undefined {
		return this.tryGetRuleContext(0, PartitionSpecContext);
	}
	public CONCATENATE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CONCATENATE, 0); }
	public FILEFORMAT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FILEFORMAT, 0); }
	public REPLACE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REPLACE, 0); }
	public COLUMNS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLUMNS, 0); }
	public START(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.START, 0); }
	public TRANSACTION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRANSACTION, 0); }
	public COMMIT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COMMIT, 0); }
	public ROLLBACK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROLLBACK, 0); }
	public DFS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DFS, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_unsupportedHiveNativeCommands; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterUnsupportedHiveNativeCommands) {
			listener.enterUnsupportedHiveNativeCommands(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitUnsupportedHiveNativeCommands) {
			listener.exitUnsupportedHiveNativeCommands(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitUnsupportedHiveNativeCommands) {
			return visitor.visitUnsupportedHiveNativeCommands(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class CreateTableHeaderContext extends ParserRuleContext {
	public CREATE(): TerminalNode { return this.getToken(SparkSqlParser.CREATE, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public TEMPORARY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TEMPORARY, 0); }
	public EXTERNAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXTERNAL, 0); }
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_createTableHeader; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCreateTableHeader) {
			listener.enterCreateTableHeader(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCreateTableHeader) {
			listener.exitCreateTableHeader(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCreateTableHeader) {
			return visitor.visitCreateTableHeader(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ReplaceTableHeaderContext extends ParserRuleContext {
	public REPLACE(): TerminalNode { return this.getToken(SparkSqlParser.REPLACE, 0); }
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public CREATE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CREATE, 0); }
	public OR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OR, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_replaceTableHeader; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterReplaceTableHeader) {
			listener.enterReplaceTableHeader(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitReplaceTableHeader) {
			listener.exitReplaceTableHeader(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitReplaceTableHeader) {
			return visitor.visitReplaceTableHeader(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class BucketSpecContext extends ParserRuleContext {
	public CLUSTERED(): TerminalNode { return this.getToken(SparkSqlParser.CLUSTERED, 0); }
	public BY(): TerminalNode[];
	public BY(i: number): TerminalNode;
	public BY(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.BY);
		} else {
			return this.getToken(SparkSqlParser.BY, i);
		}
	}
	public identifierList(): IdentifierListContext {
		return this.getRuleContext(0, IdentifierListContext);
	}
	public INTO(): TerminalNode { return this.getToken(SparkSqlParser.INTO, 0); }
	public INTEGER_VALUE(): TerminalNode { return this.getToken(SparkSqlParser.INTEGER_VALUE, 0); }
	public BUCKETS(): TerminalNode { return this.getToken(SparkSqlParser.BUCKETS, 0); }
	public SORTED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SORTED, 0); }
	public orderedIdentifierList(): OrderedIdentifierListContext | undefined {
		return this.tryGetRuleContext(0, OrderedIdentifierListContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_bucketSpec; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterBucketSpec) {
			listener.enterBucketSpec(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitBucketSpec) {
			listener.exitBucketSpec(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitBucketSpec) {
			return visitor.visitBucketSpec(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class SkewSpecContext extends ParserRuleContext {
	public SKEWED(): TerminalNode { return this.getToken(SparkSqlParser.SKEWED, 0); }
	public BY(): TerminalNode { return this.getToken(SparkSqlParser.BY, 0); }
	public identifierList(): IdentifierListContext {
		return this.getRuleContext(0, IdentifierListContext);
	}
	public ON(): TerminalNode { return this.getToken(SparkSqlParser.ON, 0); }
	public constantList(): ConstantListContext | undefined {
		return this.tryGetRuleContext(0, ConstantListContext);
	}
	public nestedConstantList(): NestedConstantListContext | undefined {
		return this.tryGetRuleContext(0, NestedConstantListContext);
	}
	public STORED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STORED, 0); }
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	public DIRECTORIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DIRECTORIES, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_skewSpec; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSkewSpec) {
			listener.enterSkewSpec(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSkewSpec) {
			listener.exitSkewSpec(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSkewSpec) {
			return visitor.visitSkewSpec(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class LocationSpecContext extends ParserRuleContext {
	public LOCATION(): TerminalNode { return this.getToken(SparkSqlParser.LOCATION, 0); }
	public STRING(): TerminalNode { return this.getToken(SparkSqlParser.STRING, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_locationSpec; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterLocationSpec) {
			listener.enterLocationSpec(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitLocationSpec) {
			listener.exitLocationSpec(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitLocationSpec) {
			return visitor.visitLocationSpec(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class CommentSpecContext extends ParserRuleContext {
	public COMMENT(): TerminalNode { return this.getToken(SparkSqlParser.COMMENT, 0); }
	public STRING(): TerminalNode { return this.getToken(SparkSqlParser.STRING, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_commentSpec; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCommentSpec) {
			listener.enterCommentSpec(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCommentSpec) {
			listener.exitCommentSpec(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCommentSpec) {
			return visitor.visitCommentSpec(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class QueryContext extends ParserRuleContext {
	public queryTerm(): QueryTermContext {
		return this.getRuleContext(0, QueryTermContext);
	}
	public queryOrganization(): QueryOrganizationContext {
		return this.getRuleContext(0, QueryOrganizationContext);
	}
	public ctes(): CtesContext | undefined {
		return this.tryGetRuleContext(0, CtesContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_query; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterQuery) {
			listener.enterQuery(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitQuery) {
			listener.exitQuery(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitQuery) {
			return visitor.visitQuery(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class InsertIntoContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_insertInto; }
	public copyFrom(ctx: InsertIntoContext): void {
		super.copyFrom(ctx);
	}
}
export class InsertOverwriteTableContext extends InsertIntoContext {
	public INSERT(): TerminalNode { return this.getToken(SparkSqlParser.INSERT, 0); }
	public OVERWRITE(): TerminalNode { return this.getToken(SparkSqlParser.OVERWRITE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public TABLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TABLE, 0); }
	public partitionSpec(): PartitionSpecContext | undefined {
		return this.tryGetRuleContext(0, PartitionSpecContext);
	}
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	constructor(ctx: InsertIntoContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterInsertOverwriteTable) {
			listener.enterInsertOverwriteTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitInsertOverwriteTable) {
			listener.exitInsertOverwriteTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitInsertOverwriteTable) {
			return visitor.visitInsertOverwriteTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class InsertIntoTableContext extends InsertIntoContext {
	public INSERT(): TerminalNode { return this.getToken(SparkSqlParser.INSERT, 0); }
	public INTO(): TerminalNode { return this.getToken(SparkSqlParser.INTO, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public TABLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TABLE, 0); }
	public partitionSpec(): PartitionSpecContext | undefined {
		return this.tryGetRuleContext(0, PartitionSpecContext);
	}
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	constructor(ctx: InsertIntoContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterInsertIntoTable) {
			listener.enterInsertIntoTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitInsertIntoTable) {
			listener.exitInsertIntoTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitInsertIntoTable) {
			return visitor.visitInsertIntoTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class InsertOverwriteHiveDirContext extends InsertIntoContext {
	public _path!: Token;
	public INSERT(): TerminalNode { return this.getToken(SparkSqlParser.INSERT, 0); }
	public OVERWRITE(): TerminalNode { return this.getToken(SparkSqlParser.OVERWRITE, 0); }
	public DIRECTORY(): TerminalNode { return this.getToken(SparkSqlParser.DIRECTORY, 0); }
	public STRING(): TerminalNode { return this.getToken(SparkSqlParser.STRING, 0); }
	public LOCAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCAL, 0); }
	public rowFormat(): RowFormatContext | undefined {
		return this.tryGetRuleContext(0, RowFormatContext);
	}
	public createFileFormat(): CreateFileFormatContext | undefined {
		return this.tryGetRuleContext(0, CreateFileFormatContext);
	}
	constructor(ctx: InsertIntoContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterInsertOverwriteHiveDir) {
			listener.enterInsertOverwriteHiveDir(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitInsertOverwriteHiveDir) {
			listener.exitInsertOverwriteHiveDir(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitInsertOverwriteHiveDir) {
			return visitor.visitInsertOverwriteHiveDir(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class InsertOverwriteDirContext extends InsertIntoContext {
	public _path!: Token;
	public INSERT(): TerminalNode { return this.getToken(SparkSqlParser.INSERT, 0); }
	public OVERWRITE(): TerminalNode { return this.getToken(SparkSqlParser.OVERWRITE, 0); }
	public DIRECTORY(): TerminalNode { return this.getToken(SparkSqlParser.DIRECTORY, 0); }
	public tableProvider(): TableProviderContext {
		return this.getRuleContext(0, TableProviderContext);
	}
	public LOCAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCAL, 0); }
	public OPTIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OPTIONS, 0); }
	public tablePropertyList(): TablePropertyListContext | undefined {
		return this.tryGetRuleContext(0, TablePropertyListContext);
	}
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	constructor(ctx: InsertIntoContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterInsertOverwriteDir) {
			listener.enterInsertOverwriteDir(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitInsertOverwriteDir) {
			listener.exitInsertOverwriteDir(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitInsertOverwriteDir) {
			return visitor.visitInsertOverwriteDir(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class PartitionSpecLocationContext extends ParserRuleContext {
	public partitionSpec(): PartitionSpecContext {
		return this.getRuleContext(0, PartitionSpecContext);
	}
	public locationSpec(): LocationSpecContext | undefined {
		return this.tryGetRuleContext(0, LocationSpecContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_partitionSpecLocation; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterPartitionSpecLocation) {
			listener.enterPartitionSpecLocation(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitPartitionSpecLocation) {
			listener.exitPartitionSpecLocation(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitPartitionSpecLocation) {
			return visitor.visitPartitionSpecLocation(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class PartitionSpecContext extends ParserRuleContext {
	public PARTITION(): TerminalNode { return this.getToken(SparkSqlParser.PARTITION, 0); }
	public partitionVal(): PartitionValContext[];
	public partitionVal(i: number): PartitionValContext;
	public partitionVal(i?: number): PartitionValContext | PartitionValContext[] {
		if (i === undefined) {
			return this.getRuleContexts(PartitionValContext);
		} else {
			return this.getRuleContext(i, PartitionValContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_partitionSpec; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterPartitionSpec) {
			listener.enterPartitionSpec(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitPartitionSpec) {
			listener.exitPartitionSpec(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitPartitionSpec) {
			return visitor.visitPartitionSpec(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class PartitionValContext extends ParserRuleContext {
	public identifier(): IdentifierContext {
		return this.getRuleContext(0, IdentifierContext);
	}
	public EQ(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EQ, 0); }
	public constant(): ConstantContext | undefined {
		return this.tryGetRuleContext(0, ConstantContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_partitionVal; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterPartitionVal) {
			listener.enterPartitionVal(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitPartitionVal) {
			listener.exitPartitionVal(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitPartitionVal) {
			return visitor.visitPartitionVal(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class NamespaceContext extends ParserRuleContext {
	public NAMESPACE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NAMESPACE, 0); }
	public DATABASE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DATABASE, 0); }
	public SCHEMA(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SCHEMA, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_namespace; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterNamespace) {
			listener.enterNamespace(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitNamespace) {
			listener.exitNamespace(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitNamespace) {
			return visitor.visitNamespace(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class DescribeFuncNameContext extends ParserRuleContext {
	public qualifiedName(): QualifiedNameContext | undefined {
		return this.tryGetRuleContext(0, QualifiedNameContext);
	}
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	public comparisonOperator(): ComparisonOperatorContext | undefined {
		return this.tryGetRuleContext(0, ComparisonOperatorContext);
	}
	public arithmeticOperator(): ArithmeticOperatorContext | undefined {
		return this.tryGetRuleContext(0, ArithmeticOperatorContext);
	}
	public predicateOperator(): PredicateOperatorContext | undefined {
		return this.tryGetRuleContext(0, PredicateOperatorContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_describeFuncName; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDescribeFuncName) {
			listener.enterDescribeFuncName(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDescribeFuncName) {
			listener.exitDescribeFuncName(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDescribeFuncName) {
			return visitor.visitDescribeFuncName(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class DescribeColNameContext extends ParserRuleContext {
	public _identifier!: IdentifierContext;
	public _nameParts: IdentifierContext[] = [];
	public identifier(): IdentifierContext[];
	public identifier(i: number): IdentifierContext;
	public identifier(i?: number): IdentifierContext | IdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(IdentifierContext);
		} else {
			return this.getRuleContext(i, IdentifierContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_describeColName; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDescribeColName) {
			listener.enterDescribeColName(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDescribeColName) {
			listener.exitDescribeColName(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDescribeColName) {
			return visitor.visitDescribeColName(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class CtesContext extends ParserRuleContext {
	public WITH(): TerminalNode { return this.getToken(SparkSqlParser.WITH, 0); }
	public namedQuery(): NamedQueryContext[];
	public namedQuery(i: number): NamedQueryContext;
	public namedQuery(i?: number): NamedQueryContext | NamedQueryContext[] {
		if (i === undefined) {
			return this.getRuleContexts(NamedQueryContext);
		} else {
			return this.getRuleContext(i, NamedQueryContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_ctes; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCtes) {
			listener.enterCtes(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCtes) {
			listener.exitCtes(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCtes) {
			return visitor.visitCtes(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class NamedQueryContext extends ParserRuleContext {
	public _name!: ErrorCapturingIdentifierContext;
	public _columnAliases!: IdentifierListContext;
	public query(): QueryContext {
		return this.getRuleContext(0, QueryContext);
	}
	public errorCapturingIdentifier(): ErrorCapturingIdentifierContext {
		return this.getRuleContext(0, ErrorCapturingIdentifierContext);
	}
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	public identifierList(): IdentifierListContext | undefined {
		return this.tryGetRuleContext(0, IdentifierListContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_namedQuery; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterNamedQuery) {
			listener.enterNamedQuery(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitNamedQuery) {
			listener.exitNamedQuery(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitNamedQuery) {
			return visitor.visitNamedQuery(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class TableProviderContext extends ParserRuleContext {
	public USING(): TerminalNode { return this.getToken(SparkSqlParser.USING, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_tableProvider; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTableProvider) {
			listener.enterTableProvider(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTableProvider) {
			listener.exitTableProvider(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTableProvider) {
			return visitor.visitTableProvider(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class CreateTableClausesContext extends ParserRuleContext {
	public _partitioning!: TransformListContext;
	public _tableProps!: TablePropertyListContext;
	public bucketSpec(): BucketSpecContext[];
	public bucketSpec(i: number): BucketSpecContext;
	public bucketSpec(i?: number): BucketSpecContext | BucketSpecContext[] {
		if (i === undefined) {
			return this.getRuleContexts(BucketSpecContext);
		} else {
			return this.getRuleContext(i, BucketSpecContext);
		}
	}
	public locationSpec(): LocationSpecContext[];
	public locationSpec(i: number): LocationSpecContext;
	public locationSpec(i?: number): LocationSpecContext | LocationSpecContext[] {
		if (i === undefined) {
			return this.getRuleContexts(LocationSpecContext);
		} else {
			return this.getRuleContext(i, LocationSpecContext);
		}
	}
	public commentSpec(): CommentSpecContext[];
	public commentSpec(i: number): CommentSpecContext;
	public commentSpec(i?: number): CommentSpecContext | CommentSpecContext[] {
		if (i === undefined) {
			return this.getRuleContexts(CommentSpecContext);
		} else {
			return this.getRuleContext(i, CommentSpecContext);
		}
	}
	public OPTIONS(): TerminalNode[];
	public OPTIONS(i: number): TerminalNode;
	public OPTIONS(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.OPTIONS);
		} else {
			return this.getToken(SparkSqlParser.OPTIONS, i);
		}
	}
	public tablePropertyList(): TablePropertyListContext[];
	public tablePropertyList(i: number): TablePropertyListContext;
	public tablePropertyList(i?: number): TablePropertyListContext | TablePropertyListContext[] {
		if (i === undefined) {
			return this.getRuleContexts(TablePropertyListContext);
		} else {
			return this.getRuleContext(i, TablePropertyListContext);
		}
	}
	public PARTITIONED(): TerminalNode[];
	public PARTITIONED(i: number): TerminalNode;
	public PARTITIONED(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.PARTITIONED);
		} else {
			return this.getToken(SparkSqlParser.PARTITIONED, i);
		}
	}
	public BY(): TerminalNode[];
	public BY(i: number): TerminalNode;
	public BY(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.BY);
		} else {
			return this.getToken(SparkSqlParser.BY, i);
		}
	}
	public TBLPROPERTIES(): TerminalNode[];
	public TBLPROPERTIES(i: number): TerminalNode;
	public TBLPROPERTIES(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.TBLPROPERTIES);
		} else {
			return this.getToken(SparkSqlParser.TBLPROPERTIES, i);
		}
	}
	public transformList(): TransformListContext[];
	public transformList(i: number): TransformListContext;
	public transformList(i?: number): TransformListContext | TransformListContext[] {
		if (i === undefined) {
			return this.getRuleContexts(TransformListContext);
		} else {
			return this.getRuleContext(i, TransformListContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_createTableClauses; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCreateTableClauses) {
			listener.enterCreateTableClauses(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCreateTableClauses) {
			listener.exitCreateTableClauses(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCreateTableClauses) {
			return visitor.visitCreateTableClauses(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class TablePropertyListContext extends ParserRuleContext {
	public tableProperty(): TablePropertyContext[];
	public tableProperty(i: number): TablePropertyContext;
	public tableProperty(i?: number): TablePropertyContext | TablePropertyContext[] {
		if (i === undefined) {
			return this.getRuleContexts(TablePropertyContext);
		} else {
			return this.getRuleContext(i, TablePropertyContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_tablePropertyList; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTablePropertyList) {
			listener.enterTablePropertyList(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTablePropertyList) {
			listener.exitTablePropertyList(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTablePropertyList) {
			return visitor.visitTablePropertyList(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class TablePropertyContext extends ParserRuleContext {
	public _key!: TablePropertyKeyContext;
	public _value!: TablePropertyValueContext;
	public tablePropertyKey(): TablePropertyKeyContext {
		return this.getRuleContext(0, TablePropertyKeyContext);
	}
	public tablePropertyValue(): TablePropertyValueContext | undefined {
		return this.tryGetRuleContext(0, TablePropertyValueContext);
	}
	public EQ(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EQ, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_tableProperty; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTableProperty) {
			listener.enterTableProperty(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTableProperty) {
			listener.exitTableProperty(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTableProperty) {
			return visitor.visitTableProperty(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class TablePropertyKeyContext extends ParserRuleContext {
	public identifier(): IdentifierContext[];
	public identifier(i: number): IdentifierContext;
	public identifier(i?: number): IdentifierContext | IdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(IdentifierContext);
		} else {
			return this.getRuleContext(i, IdentifierContext);
		}
	}
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_tablePropertyKey; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTablePropertyKey) {
			listener.enterTablePropertyKey(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTablePropertyKey) {
			listener.exitTablePropertyKey(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTablePropertyKey) {
			return visitor.visitTablePropertyKey(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class TablePropertyValueContext extends ParserRuleContext {
	public INTEGER_VALUE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INTEGER_VALUE, 0); }
	public DECIMAL_VALUE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DECIMAL_VALUE, 0); }
	public booleanValue(): BooleanValueContext | undefined {
		return this.tryGetRuleContext(0, BooleanValueContext);
	}
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_tablePropertyValue; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTablePropertyValue) {
			listener.enterTablePropertyValue(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTablePropertyValue) {
			listener.exitTablePropertyValue(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTablePropertyValue) {
			return visitor.visitTablePropertyValue(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ConstantListContext extends ParserRuleContext {
	public constant(): ConstantContext[];
	public constant(i: number): ConstantContext;
	public constant(i?: number): ConstantContext | ConstantContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ConstantContext);
		} else {
			return this.getRuleContext(i, ConstantContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_constantList; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterConstantList) {
			listener.enterConstantList(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitConstantList) {
			listener.exitConstantList(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitConstantList) {
			return visitor.visitConstantList(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class NestedConstantListContext extends ParserRuleContext {
	public constantList(): ConstantListContext[];
	public constantList(i: number): ConstantListContext;
	public constantList(i?: number): ConstantListContext | ConstantListContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ConstantListContext);
		} else {
			return this.getRuleContext(i, ConstantListContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_nestedConstantList; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterNestedConstantList) {
			listener.enterNestedConstantList(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitNestedConstantList) {
			listener.exitNestedConstantList(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitNestedConstantList) {
			return visitor.visitNestedConstantList(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class CreateFileFormatContext extends ParserRuleContext {
	public STORED(): TerminalNode { return this.getToken(SparkSqlParser.STORED, 0); }
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	public fileFormat(): FileFormatContext | undefined {
		return this.tryGetRuleContext(0, FileFormatContext);
	}
	public BY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.BY, 0); }
	public storageHandler(): StorageHandlerContext | undefined {
		return this.tryGetRuleContext(0, StorageHandlerContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_createFileFormat; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCreateFileFormat) {
			listener.enterCreateFileFormat(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCreateFileFormat) {
			listener.exitCreateFileFormat(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCreateFileFormat) {
			return visitor.visitCreateFileFormat(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class FileFormatContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_fileFormat; }
	public copyFrom(ctx: FileFormatContext): void {
		super.copyFrom(ctx);
	}
}
export class TableFileFormatContext extends FileFormatContext {
	public _inFmt!: Token;
	public _outFmt!: Token;
	public INPUTFORMAT(): TerminalNode { return this.getToken(SparkSqlParser.INPUTFORMAT, 0); }
	public OUTPUTFORMAT(): TerminalNode { return this.getToken(SparkSqlParser.OUTPUTFORMAT, 0); }
	public STRING(): TerminalNode[];
	public STRING(i: number): TerminalNode;
	public STRING(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.STRING);
		} else {
			return this.getToken(SparkSqlParser.STRING, i);
		}
	}
	constructor(ctx: FileFormatContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTableFileFormat) {
			listener.enterTableFileFormat(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTableFileFormat) {
			listener.exitTableFileFormat(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTableFileFormat) {
			return visitor.visitTableFileFormat(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class GenericFileFormatContext extends FileFormatContext {
	public identifier(): IdentifierContext {
		return this.getRuleContext(0, IdentifierContext);
	}
	constructor(ctx: FileFormatContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterGenericFileFormat) {
			listener.enterGenericFileFormat(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitGenericFileFormat) {
			listener.exitGenericFileFormat(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitGenericFileFormat) {
			return visitor.visitGenericFileFormat(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class StorageHandlerContext extends ParserRuleContext {
	public STRING(): TerminalNode { return this.getToken(SparkSqlParser.STRING, 0); }
	public WITH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.WITH, 0); }
	public SERDEPROPERTIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SERDEPROPERTIES, 0); }
	public tablePropertyList(): TablePropertyListContext | undefined {
		return this.tryGetRuleContext(0, TablePropertyListContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_storageHandler; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterStorageHandler) {
			listener.enterStorageHandler(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitStorageHandler) {
			listener.exitStorageHandler(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitStorageHandler) {
			return visitor.visitStorageHandler(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ResourceContext extends ParserRuleContext {
	public identifier(): IdentifierContext {
		return this.getRuleContext(0, IdentifierContext);
	}
	public STRING(): TerminalNode { return this.getToken(SparkSqlParser.STRING, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_resource; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterResource) {
			listener.enterResource(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitResource) {
			listener.exitResource(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitResource) {
			return visitor.visitResource(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class DmlStatementNoWithContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_dmlStatementNoWith; }
	public copyFrom(ctx: DmlStatementNoWithContext): void {
		super.copyFrom(ctx);
	}
}
export class SingleInsertQueryContext extends DmlStatementNoWithContext {
	public insertInto(): InsertIntoContext {
		return this.getRuleContext(0, InsertIntoContext);
	}
	public queryTerm(): QueryTermContext {
		return this.getRuleContext(0, QueryTermContext);
	}
	public queryOrganization(): QueryOrganizationContext {
		return this.getRuleContext(0, QueryOrganizationContext);
	}
	constructor(ctx: DmlStatementNoWithContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSingleInsertQuery) {
			listener.enterSingleInsertQuery(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSingleInsertQuery) {
			listener.exitSingleInsertQuery(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSingleInsertQuery) {
			return visitor.visitSingleInsertQuery(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class MultiInsertQueryContext extends DmlStatementNoWithContext {
	public fromClause(): FromClauseContext {
		return this.getRuleContext(0, FromClauseContext);
	}
	public multiInsertQueryBody(): MultiInsertQueryBodyContext[];
	public multiInsertQueryBody(i: number): MultiInsertQueryBodyContext;
	public multiInsertQueryBody(i?: number): MultiInsertQueryBodyContext | MultiInsertQueryBodyContext[] {
		if (i === undefined) {
			return this.getRuleContexts(MultiInsertQueryBodyContext);
		} else {
			return this.getRuleContext(i, MultiInsertQueryBodyContext);
		}
	}
	constructor(ctx: DmlStatementNoWithContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterMultiInsertQuery) {
			listener.enterMultiInsertQuery(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitMultiInsertQuery) {
			listener.exitMultiInsertQuery(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitMultiInsertQuery) {
			return visitor.visitMultiInsertQuery(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DeleteFromTableContext extends DmlStatementNoWithContext {
	public DELETE(): TerminalNode { return this.getToken(SparkSqlParser.DELETE, 0); }
	public FROM(): TerminalNode { return this.getToken(SparkSqlParser.FROM, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public tableAlias(): TableAliasContext {
		return this.getRuleContext(0, TableAliasContext);
	}
	public whereClause(): WhereClauseContext | undefined {
		return this.tryGetRuleContext(0, WhereClauseContext);
	}
	constructor(ctx: DmlStatementNoWithContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDeleteFromTable) {
			listener.enterDeleteFromTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDeleteFromTable) {
			listener.exitDeleteFromTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDeleteFromTable) {
			return visitor.visitDeleteFromTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class UpdateTableContext extends DmlStatementNoWithContext {
	public UPDATE(): TerminalNode { return this.getToken(SparkSqlParser.UPDATE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public tableAlias(): TableAliasContext {
		return this.getRuleContext(0, TableAliasContext);
	}
	public setClause(): SetClauseContext {
		return this.getRuleContext(0, SetClauseContext);
	}
	public whereClause(): WhereClauseContext | undefined {
		return this.tryGetRuleContext(0, WhereClauseContext);
	}
	constructor(ctx: DmlStatementNoWithContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterUpdateTable) {
			listener.enterUpdateTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitUpdateTable) {
			listener.exitUpdateTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitUpdateTable) {
			return visitor.visitUpdateTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class MergeIntoTableContext extends DmlStatementNoWithContext {
	public _target!: MultipartIdentifierContext;
	public _targetAlias!: TableAliasContext;
	public _source!: MultipartIdentifierContext;
	public _sourceQuery!: QueryContext;
	public _sourceAlias!: TableAliasContext;
	public _mergeCondition!: BooleanExpressionContext;
	public MERGE(): TerminalNode { return this.getToken(SparkSqlParser.MERGE, 0); }
	public INTO(): TerminalNode { return this.getToken(SparkSqlParser.INTO, 0); }
	public USING(): TerminalNode { return this.getToken(SparkSqlParser.USING, 0); }
	public ON(): TerminalNode { return this.getToken(SparkSqlParser.ON, 0); }
	public multipartIdentifier(): MultipartIdentifierContext[];
	public multipartIdentifier(i: number): MultipartIdentifierContext;
	public multipartIdentifier(i?: number): MultipartIdentifierContext | MultipartIdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(MultipartIdentifierContext);
		} else {
			return this.getRuleContext(i, MultipartIdentifierContext);
		}
	}
	public tableAlias(): TableAliasContext[];
	public tableAlias(i: number): TableAliasContext;
	public tableAlias(i?: number): TableAliasContext | TableAliasContext[] {
		if (i === undefined) {
			return this.getRuleContexts(TableAliasContext);
		} else {
			return this.getRuleContext(i, TableAliasContext);
		}
	}
	public booleanExpression(): BooleanExpressionContext {
		return this.getRuleContext(0, BooleanExpressionContext);
	}
	public query(): QueryContext | undefined {
		return this.tryGetRuleContext(0, QueryContext);
	}
	public matchedClause(): MatchedClauseContext[];
	public matchedClause(i: number): MatchedClauseContext;
	public matchedClause(i?: number): MatchedClauseContext | MatchedClauseContext[] {
		if (i === undefined) {
			return this.getRuleContexts(MatchedClauseContext);
		} else {
			return this.getRuleContext(i, MatchedClauseContext);
		}
	}
	public notMatchedClause(): NotMatchedClauseContext[];
	public notMatchedClause(i: number): NotMatchedClauseContext;
	public notMatchedClause(i?: number): NotMatchedClauseContext | NotMatchedClauseContext[] {
		if (i === undefined) {
			return this.getRuleContexts(NotMatchedClauseContext);
		} else {
			return this.getRuleContext(i, NotMatchedClauseContext);
		}
	}
	constructor(ctx: DmlStatementNoWithContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterMergeIntoTable) {
			listener.enterMergeIntoTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitMergeIntoTable) {
			listener.exitMergeIntoTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitMergeIntoTable) {
			return visitor.visitMergeIntoTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class QueryOrganizationContext extends ParserRuleContext {
	public _sortItem!: SortItemContext;
	public _order: SortItemContext[] = [];
	public _expression!: ExpressionContext;
	public _clusterBy: ExpressionContext[] = [];
	public _distributeBy: ExpressionContext[] = [];
	public _sort: SortItemContext[] = [];
	public _limit!: ExpressionContext;
	public ORDER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ORDER, 0); }
	public BY(): TerminalNode[];
	public BY(i: number): TerminalNode;
	public BY(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.BY);
		} else {
			return this.getToken(SparkSqlParser.BY, i);
		}
	}
	public CLUSTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CLUSTER, 0); }
	public DISTRIBUTE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DISTRIBUTE, 0); }
	public SORT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SORT, 0); }
	public windowClause(): WindowClauseContext | undefined {
		return this.tryGetRuleContext(0, WindowClauseContext);
	}
	public LIMIT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LIMIT, 0); }
	public sortItem(): SortItemContext[];
	public sortItem(i: number): SortItemContext;
	public sortItem(i?: number): SortItemContext | SortItemContext[] {
		if (i === undefined) {
			return this.getRuleContexts(SortItemContext);
		} else {
			return this.getRuleContext(i, SortItemContext);
		}
	}
	public expression(): ExpressionContext[];
	public expression(i: number): ExpressionContext;
	public expression(i?: number): ExpressionContext | ExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ExpressionContext);
		} else {
			return this.getRuleContext(i, ExpressionContext);
		}
	}
	public ALL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ALL, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_queryOrganization; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterQueryOrganization) {
			listener.enterQueryOrganization(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitQueryOrganization) {
			listener.exitQueryOrganization(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitQueryOrganization) {
			return visitor.visitQueryOrganization(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class MultiInsertQueryBodyContext extends ParserRuleContext {
	public insertInto(): InsertIntoContext {
		return this.getRuleContext(0, InsertIntoContext);
	}
	public fromStatementBody(): FromStatementBodyContext {
		return this.getRuleContext(0, FromStatementBodyContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_multiInsertQueryBody; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterMultiInsertQueryBody) {
			listener.enterMultiInsertQueryBody(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitMultiInsertQueryBody) {
			listener.exitMultiInsertQueryBody(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitMultiInsertQueryBody) {
			return visitor.visitMultiInsertQueryBody(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class QueryTermContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_queryTerm; }
	public copyFrom(ctx: QueryTermContext): void {
		super.copyFrom(ctx);
	}
}
export class QueryTermDefaultContext extends QueryTermContext {
	public queryPrimary(): QueryPrimaryContext {
		return this.getRuleContext(0, QueryPrimaryContext);
	}
	constructor(ctx: QueryTermContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterQueryTermDefault) {
			listener.enterQueryTermDefault(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitQueryTermDefault) {
			listener.exitQueryTermDefault(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitQueryTermDefault) {
			return visitor.visitQueryTermDefault(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SetOperationContext extends QueryTermContext {
	public _left!: QueryTermContext;
	public _operator!: Token;
	public _right!: QueryTermContext;
	public queryTerm(): QueryTermContext[];
	public queryTerm(i: number): QueryTermContext;
	public queryTerm(i?: number): QueryTermContext | QueryTermContext[] {
		if (i === undefined) {
			return this.getRuleContexts(QueryTermContext);
		} else {
			return this.getRuleContext(i, QueryTermContext);
		}
	}
	public INTERSECT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INTERSECT, 0); }
	public UNION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNION, 0); }
	public EXCEPT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXCEPT, 0); }
	public SETMINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SETMINUS, 0); }
	public setQuantifier(): SetQuantifierContext | undefined {
		return this.tryGetRuleContext(0, SetQuantifierContext);
	}
	constructor(ctx: QueryTermContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSetOperation) {
			listener.enterSetOperation(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSetOperation) {
			listener.exitSetOperation(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSetOperation) {
			return visitor.visitSetOperation(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class QueryPrimaryContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_queryPrimary; }
	public copyFrom(ctx: QueryPrimaryContext): void {
		super.copyFrom(ctx);
	}
}
export class QueryPrimaryDefaultContext extends QueryPrimaryContext {
	public querySpecification(): QuerySpecificationContext {
		return this.getRuleContext(0, QuerySpecificationContext);
	}
	constructor(ctx: QueryPrimaryContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterQueryPrimaryDefault) {
			listener.enterQueryPrimaryDefault(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitQueryPrimaryDefault) {
			listener.exitQueryPrimaryDefault(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitQueryPrimaryDefault) {
			return visitor.visitQueryPrimaryDefault(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class FromStmtContext extends QueryPrimaryContext {
	public fromStatement(): FromStatementContext {
		return this.getRuleContext(0, FromStatementContext);
	}
	constructor(ctx: QueryPrimaryContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterFromStmt) {
			listener.enterFromStmt(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitFromStmt) {
			listener.exitFromStmt(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitFromStmt) {
			return visitor.visitFromStmt(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class TableContext extends QueryPrimaryContext {
	public TABLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLE, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	constructor(ctx: QueryPrimaryContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTable) {
			listener.enterTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTable) {
			listener.exitTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTable) {
			return visitor.visitTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class InlineTableDefault1Context extends QueryPrimaryContext {
	public inlineTable(): InlineTableContext {
		return this.getRuleContext(0, InlineTableContext);
	}
	constructor(ctx: QueryPrimaryContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterInlineTableDefault1) {
			listener.enterInlineTableDefault1(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitInlineTableDefault1) {
			listener.exitInlineTableDefault1(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitInlineTableDefault1) {
			return visitor.visitInlineTableDefault1(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SubqueryContext extends QueryPrimaryContext {
	public query(): QueryContext {
		return this.getRuleContext(0, QueryContext);
	}
	constructor(ctx: QueryPrimaryContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSubquery) {
			listener.enterSubquery(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSubquery) {
			listener.exitSubquery(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSubquery) {
			return visitor.visitSubquery(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class SortItemContext extends ParserRuleContext {
	public _ordering!: Token;
	public _nullOrder!: Token;
	public expression(): ExpressionContext {
		return this.getRuleContext(0, ExpressionContext);
	}
	public NULLS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NULLS, 0); }
	public ASC(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ASC, 0); }
	public DESC(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DESC, 0); }
	public LAST(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LAST, 0); }
	public FIRST(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FIRST, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_sortItem; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSortItem) {
			listener.enterSortItem(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSortItem) {
			listener.exitSortItem(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSortItem) {
			return visitor.visitSortItem(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class FromStatementContext extends ParserRuleContext {
	public fromClause(): FromClauseContext {
		return this.getRuleContext(0, FromClauseContext);
	}
	public fromStatementBody(): FromStatementBodyContext[];
	public fromStatementBody(i: number): FromStatementBodyContext;
	public fromStatementBody(i?: number): FromStatementBodyContext | FromStatementBodyContext[] {
		if (i === undefined) {
			return this.getRuleContexts(FromStatementBodyContext);
		} else {
			return this.getRuleContext(i, FromStatementBodyContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_fromStatement; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterFromStatement) {
			listener.enterFromStatement(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitFromStatement) {
			listener.exitFromStatement(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitFromStatement) {
			return visitor.visitFromStatement(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class FromStatementBodyContext extends ParserRuleContext {
	public transformClause(): TransformClauseContext | undefined {
		return this.tryGetRuleContext(0, TransformClauseContext);
	}
	public queryOrganization(): QueryOrganizationContext {
		return this.getRuleContext(0, QueryOrganizationContext);
	}
	public whereClause(): WhereClauseContext | undefined {
		return this.tryGetRuleContext(0, WhereClauseContext);
	}
	public selectClause(): SelectClauseContext | undefined {
		return this.tryGetRuleContext(0, SelectClauseContext);
	}
	public lateralView(): LateralViewContext[];
	public lateralView(i: number): LateralViewContext;
	public lateralView(i?: number): LateralViewContext | LateralViewContext[] {
		if (i === undefined) {
			return this.getRuleContexts(LateralViewContext);
		} else {
			return this.getRuleContext(i, LateralViewContext);
		}
	}
	public aggregationClause(): AggregationClauseContext | undefined {
		return this.tryGetRuleContext(0, AggregationClauseContext);
	}
	public havingClause(): HavingClauseContext | undefined {
		return this.tryGetRuleContext(0, HavingClauseContext);
	}
	public windowClause(): WindowClauseContext | undefined {
		return this.tryGetRuleContext(0, WindowClauseContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_fromStatementBody; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterFromStatementBody) {
			listener.enterFromStatementBody(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitFromStatementBody) {
			listener.exitFromStatementBody(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitFromStatementBody) {
			return visitor.visitFromStatementBody(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class QuerySpecificationContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_querySpecification; }
	public copyFrom(ctx: QuerySpecificationContext): void {
		super.copyFrom(ctx);
	}
}
export class TransformQuerySpecificationContext extends QuerySpecificationContext {
	public transformClause(): TransformClauseContext {
		return this.getRuleContext(0, TransformClauseContext);
	}
	public fromClause(): FromClauseContext | undefined {
		return this.tryGetRuleContext(0, FromClauseContext);
	}
	public whereClause(): WhereClauseContext | undefined {
		return this.tryGetRuleContext(0, WhereClauseContext);
	}
	constructor(ctx: QuerySpecificationContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTransformQuerySpecification) {
			listener.enterTransformQuerySpecification(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTransformQuerySpecification) {
			listener.exitTransformQuerySpecification(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTransformQuerySpecification) {
			return visitor.visitTransformQuerySpecification(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class RegularQuerySpecificationContext extends QuerySpecificationContext {
	public selectClause(): SelectClauseContext {
		return this.getRuleContext(0, SelectClauseContext);
	}
	public fromClause(): FromClauseContext | undefined {
		return this.tryGetRuleContext(0, FromClauseContext);
	}
	public lateralView(): LateralViewContext[];
	public lateralView(i: number): LateralViewContext;
	public lateralView(i?: number): LateralViewContext | LateralViewContext[] {
		if (i === undefined) {
			return this.getRuleContexts(LateralViewContext);
		} else {
			return this.getRuleContext(i, LateralViewContext);
		}
	}
	public whereClause(): WhereClauseContext | undefined {
		return this.tryGetRuleContext(0, WhereClauseContext);
	}
	public aggregationClause(): AggregationClauseContext | undefined {
		return this.tryGetRuleContext(0, AggregationClauseContext);
	}
	public havingClause(): HavingClauseContext | undefined {
		return this.tryGetRuleContext(0, HavingClauseContext);
	}
	public windowClause(): WindowClauseContext | undefined {
		return this.tryGetRuleContext(0, WindowClauseContext);
	}
	constructor(ctx: QuerySpecificationContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterRegularQuerySpecification) {
			listener.enterRegularQuerySpecification(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitRegularQuerySpecification) {
			listener.exitRegularQuerySpecification(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitRegularQuerySpecification) {
			return visitor.visitRegularQuerySpecification(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class TransformClauseContext extends ParserRuleContext {
	public _kind!: Token;
	public _inRowFormat!: RowFormatContext;
	public _recordWriter!: Token;
	public _script!: Token;
	public _outRowFormat!: RowFormatContext;
	public _recordReader!: Token;
	public USING(): TerminalNode { return this.getToken(SparkSqlParser.USING, 0); }
	public STRING(): TerminalNode[];
	public STRING(i: number): TerminalNode;
	public STRING(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.STRING);
		} else {
			return this.getToken(SparkSqlParser.STRING, i);
		}
	}
	public SELECT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SELECT, 0); }
	public namedExpressionSeq(): NamedExpressionSeqContext | undefined {
		return this.tryGetRuleContext(0, NamedExpressionSeqContext);
	}
	public TRANSFORM(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRANSFORM, 0); }
	public MAP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MAP, 0); }
	public REDUCE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REDUCE, 0); }
	public RECORDWRITER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RECORDWRITER, 0); }
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	public RECORDREADER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RECORDREADER, 0); }
	public rowFormat(): RowFormatContext[];
	public rowFormat(i: number): RowFormatContext;
	public rowFormat(i?: number): RowFormatContext | RowFormatContext[] {
		if (i === undefined) {
			return this.getRuleContexts(RowFormatContext);
		} else {
			return this.getRuleContext(i, RowFormatContext);
		}
	}
	public identifierSeq(): IdentifierSeqContext | undefined {
		return this.tryGetRuleContext(0, IdentifierSeqContext);
	}
	public colTypeList(): ColTypeListContext | undefined {
		return this.tryGetRuleContext(0, ColTypeListContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_transformClause; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTransformClause) {
			listener.enterTransformClause(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTransformClause) {
			listener.exitTransformClause(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTransformClause) {
			return visitor.visitTransformClause(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class SelectClauseContext extends ParserRuleContext {
	public _hint!: HintContext;
	public _hints: HintContext[] = [];
	public SELECT(): TerminalNode { return this.getToken(SparkSqlParser.SELECT, 0); }
	public namedExpressionSeq(): NamedExpressionSeqContext {
		return this.getRuleContext(0, NamedExpressionSeqContext);
	}
	public setQuantifier(): SetQuantifierContext | undefined {
		return this.tryGetRuleContext(0, SetQuantifierContext);
	}
	public hint(): HintContext[];
	public hint(i: number): HintContext;
	public hint(i?: number): HintContext | HintContext[] {
		if (i === undefined) {
			return this.getRuleContexts(HintContext);
		} else {
			return this.getRuleContext(i, HintContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_selectClause; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSelectClause) {
			listener.enterSelectClause(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSelectClause) {
			listener.exitSelectClause(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSelectClause) {
			return visitor.visitSelectClause(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class SetClauseContext extends ParserRuleContext {
	public SET(): TerminalNode { return this.getToken(SparkSqlParser.SET, 0); }
	public assignmentList(): AssignmentListContext {
		return this.getRuleContext(0, AssignmentListContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_setClause; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSetClause) {
			listener.enterSetClause(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSetClause) {
			listener.exitSetClause(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSetClause) {
			return visitor.visitSetClause(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class MatchedClauseContext extends ParserRuleContext {
	public _matchedCond!: BooleanExpressionContext;
	public WHEN(): TerminalNode { return this.getToken(SparkSqlParser.WHEN, 0); }
	public MATCHED(): TerminalNode { return this.getToken(SparkSqlParser.MATCHED, 0); }
	public THEN(): TerminalNode { return this.getToken(SparkSqlParser.THEN, 0); }
	public matchedAction(): MatchedActionContext {
		return this.getRuleContext(0, MatchedActionContext);
	}
	public AND(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AND, 0); }
	public booleanExpression(): BooleanExpressionContext | undefined {
		return this.tryGetRuleContext(0, BooleanExpressionContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_matchedClause; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterMatchedClause) {
			listener.enterMatchedClause(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitMatchedClause) {
			listener.exitMatchedClause(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitMatchedClause) {
			return visitor.visitMatchedClause(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class NotMatchedClauseContext extends ParserRuleContext {
	public _notMatchedCond!: BooleanExpressionContext;
	public WHEN(): TerminalNode { return this.getToken(SparkSqlParser.WHEN, 0); }
	public NOT(): TerminalNode { return this.getToken(SparkSqlParser.NOT, 0); }
	public MATCHED(): TerminalNode { return this.getToken(SparkSqlParser.MATCHED, 0); }
	public THEN(): TerminalNode { return this.getToken(SparkSqlParser.THEN, 0); }
	public notMatchedAction(): NotMatchedActionContext {
		return this.getRuleContext(0, NotMatchedActionContext);
	}
	public AND(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AND, 0); }
	public booleanExpression(): BooleanExpressionContext | undefined {
		return this.tryGetRuleContext(0, BooleanExpressionContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_notMatchedClause; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterNotMatchedClause) {
			listener.enterNotMatchedClause(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitNotMatchedClause) {
			listener.exitNotMatchedClause(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitNotMatchedClause) {
			return visitor.visitNotMatchedClause(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class MatchedActionContext extends ParserRuleContext {
	public DELETE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DELETE, 0); }
	public UPDATE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UPDATE, 0); }
	public SET(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SET, 0); }
	public ASTERISK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ASTERISK, 0); }
	public assignmentList(): AssignmentListContext | undefined {
		return this.tryGetRuleContext(0, AssignmentListContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_matchedAction; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterMatchedAction) {
			listener.enterMatchedAction(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitMatchedAction) {
			listener.exitMatchedAction(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitMatchedAction) {
			return visitor.visitMatchedAction(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class NotMatchedActionContext extends ParserRuleContext {
	public _columns!: MultipartIdentifierListContext;
	public INSERT(): TerminalNode { return this.getToken(SparkSqlParser.INSERT, 0); }
	public ASTERISK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ASTERISK, 0); }
	public VALUES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.VALUES, 0); }
	public expression(): ExpressionContext[];
	public expression(i: number): ExpressionContext;
	public expression(i?: number): ExpressionContext | ExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ExpressionContext);
		} else {
			return this.getRuleContext(i, ExpressionContext);
		}
	}
	public multipartIdentifierList(): MultipartIdentifierListContext | undefined {
		return this.tryGetRuleContext(0, MultipartIdentifierListContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_notMatchedAction; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterNotMatchedAction) {
			listener.enterNotMatchedAction(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitNotMatchedAction) {
			listener.exitNotMatchedAction(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitNotMatchedAction) {
			return visitor.visitNotMatchedAction(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class AssignmentListContext extends ParserRuleContext {
	public assignment(): AssignmentContext[];
	public assignment(i: number): AssignmentContext;
	public assignment(i?: number): AssignmentContext | AssignmentContext[] {
		if (i === undefined) {
			return this.getRuleContexts(AssignmentContext);
		} else {
			return this.getRuleContext(i, AssignmentContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_assignmentList; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterAssignmentList) {
			listener.enterAssignmentList(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitAssignmentList) {
			listener.exitAssignmentList(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitAssignmentList) {
			return visitor.visitAssignmentList(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class AssignmentContext extends ParserRuleContext {
	public _key!: MultipartIdentifierContext;
	public _value!: ExpressionContext;
	public EQ(): TerminalNode { return this.getToken(SparkSqlParser.EQ, 0); }
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public expression(): ExpressionContext {
		return this.getRuleContext(0, ExpressionContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_assignment; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterAssignment) {
			listener.enterAssignment(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitAssignment) {
			listener.exitAssignment(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitAssignment) {
			return visitor.visitAssignment(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class WhereClauseContext extends ParserRuleContext {
	public WHERE(): TerminalNode { return this.getToken(SparkSqlParser.WHERE, 0); }
	public booleanExpression(): BooleanExpressionContext {
		return this.getRuleContext(0, BooleanExpressionContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_whereClause; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterWhereClause) {
			listener.enterWhereClause(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitWhereClause) {
			listener.exitWhereClause(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitWhereClause) {
			return visitor.visitWhereClause(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class HavingClauseContext extends ParserRuleContext {
	public HAVING(): TerminalNode { return this.getToken(SparkSqlParser.HAVING, 0); }
	public booleanExpression(): BooleanExpressionContext {
		return this.getRuleContext(0, BooleanExpressionContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_havingClause; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterHavingClause) {
			listener.enterHavingClause(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitHavingClause) {
			listener.exitHavingClause(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitHavingClause) {
			return visitor.visitHavingClause(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class HintContext extends ParserRuleContext {
	public _hintStatement!: HintStatementContext;
	public _hintStatements: HintStatementContext[] = [];
	public hintStatement(): HintStatementContext[];
	public hintStatement(i: number): HintStatementContext;
	public hintStatement(i?: number): HintStatementContext | HintStatementContext[] {
		if (i === undefined) {
			return this.getRuleContexts(HintStatementContext);
		} else {
			return this.getRuleContext(i, HintStatementContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_hint; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterHint) {
			listener.enterHint(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitHint) {
			listener.exitHint(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitHint) {
			return visitor.visitHint(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class HintStatementContext extends ParserRuleContext {
	public _hintName!: IdentifierContext;
	public _primaryExpression!: PrimaryExpressionContext;
	public _parameters: PrimaryExpressionContext[] = [];
	public identifier(): IdentifierContext {
		return this.getRuleContext(0, IdentifierContext);
	}
	public primaryExpression(): PrimaryExpressionContext[];
	public primaryExpression(i: number): PrimaryExpressionContext;
	public primaryExpression(i?: number): PrimaryExpressionContext | PrimaryExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(PrimaryExpressionContext);
		} else {
			return this.getRuleContext(i, PrimaryExpressionContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_hintStatement; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterHintStatement) {
			listener.enterHintStatement(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitHintStatement) {
			listener.exitHintStatement(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitHintStatement) {
			return visitor.visitHintStatement(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class FromClauseContext extends ParserRuleContext {
	public FROM(): TerminalNode { return this.getToken(SparkSqlParser.FROM, 0); }
	public relation(): RelationContext[];
	public relation(i: number): RelationContext;
	public relation(i?: number): RelationContext | RelationContext[] {
		if (i === undefined) {
			return this.getRuleContexts(RelationContext);
		} else {
			return this.getRuleContext(i, RelationContext);
		}
	}
	public suggestionRelation(): SuggestionRelationContext[];
	public suggestionRelation(i: number): SuggestionRelationContext;
	public suggestionRelation(i?: number): SuggestionRelationContext | SuggestionRelationContext[] {
		if (i === undefined) {
			return this.getRuleContexts(SuggestionRelationContext);
		} else {
			return this.getRuleContext(i, SuggestionRelationContext);
		}
	}
	public lateralView(): LateralViewContext[];
	public lateralView(i: number): LateralViewContext;
	public lateralView(i?: number): LateralViewContext | LateralViewContext[] {
		if (i === undefined) {
			return this.getRuleContexts(LateralViewContext);
		} else {
			return this.getRuleContext(i, LateralViewContext);
		}
	}
	public pivotClause(): PivotClauseContext | undefined {
		return this.tryGetRuleContext(0, PivotClauseContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_fromClause; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterFromClause) {
			listener.enterFromClause(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitFromClause) {
			listener.exitFromClause(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitFromClause) {
			return visitor.visitFromClause(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class SuggestionRelationContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_suggestionRelation; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSuggestionRelation) {
			listener.enterSuggestionRelation(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSuggestionRelation) {
			listener.exitSuggestionRelation(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSuggestionRelation) {
			return visitor.visitSuggestionRelation(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class AggregationClauseContext extends ParserRuleContext {
	public _expression!: ExpressionContext;
	public _groupingExpressions: ExpressionContext[] = [];
	public _kind!: Token;
	public GROUP(): TerminalNode { return this.getToken(SparkSqlParser.GROUP, 0); }
	public BY(): TerminalNode { return this.getToken(SparkSqlParser.BY, 0); }
	public expression(): ExpressionContext[];
	public expression(i: number): ExpressionContext;
	public expression(i?: number): ExpressionContext | ExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ExpressionContext);
		} else {
			return this.getRuleContext(i, ExpressionContext);
		}
	}
	public WITH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.WITH, 0); }
	public SETS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SETS, 0); }
	public groupingSet(): GroupingSetContext[];
	public groupingSet(i: number): GroupingSetContext;
	public groupingSet(i?: number): GroupingSetContext | GroupingSetContext[] {
		if (i === undefined) {
			return this.getRuleContexts(GroupingSetContext);
		} else {
			return this.getRuleContext(i, GroupingSetContext);
		}
	}
	public ROLLUP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROLLUP, 0); }
	public CUBE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CUBE, 0); }
	public GROUPING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.GROUPING, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_aggregationClause; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterAggregationClause) {
			listener.enterAggregationClause(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitAggregationClause) {
			listener.exitAggregationClause(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitAggregationClause) {
			return visitor.visitAggregationClause(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class GroupingSetContext extends ParserRuleContext {
	public expression(): ExpressionContext[];
	public expression(i: number): ExpressionContext;
	public expression(i?: number): ExpressionContext | ExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ExpressionContext);
		} else {
			return this.getRuleContext(i, ExpressionContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_groupingSet; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterGroupingSet) {
			listener.enterGroupingSet(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitGroupingSet) {
			listener.exitGroupingSet(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitGroupingSet) {
			return visitor.visitGroupingSet(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class PivotClauseContext extends ParserRuleContext {
	public _aggregates!: NamedExpressionSeqContext;
	public _pivotValue!: PivotValueContext;
	public _pivotValues: PivotValueContext[] = [];
	public PIVOT(): TerminalNode { return this.getToken(SparkSqlParser.PIVOT, 0); }
	public FOR(): TerminalNode { return this.getToken(SparkSqlParser.FOR, 0); }
	public pivotColumn(): PivotColumnContext {
		return this.getRuleContext(0, PivotColumnContext);
	}
	public IN(): TerminalNode { return this.getToken(SparkSqlParser.IN, 0); }
	public namedExpressionSeq(): NamedExpressionSeqContext {
		return this.getRuleContext(0, NamedExpressionSeqContext);
	}
	public pivotValue(): PivotValueContext[];
	public pivotValue(i: number): PivotValueContext;
	public pivotValue(i?: number): PivotValueContext | PivotValueContext[] {
		if (i === undefined) {
			return this.getRuleContexts(PivotValueContext);
		} else {
			return this.getRuleContext(i, PivotValueContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_pivotClause; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterPivotClause) {
			listener.enterPivotClause(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitPivotClause) {
			listener.exitPivotClause(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitPivotClause) {
			return visitor.visitPivotClause(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class PivotColumnContext extends ParserRuleContext {
	public _identifier!: IdentifierContext;
	public _identifiers: IdentifierContext[] = [];
	public identifier(): IdentifierContext[];
	public identifier(i: number): IdentifierContext;
	public identifier(i?: number): IdentifierContext | IdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(IdentifierContext);
		} else {
			return this.getRuleContext(i, IdentifierContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_pivotColumn; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterPivotColumn) {
			listener.enterPivotColumn(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitPivotColumn) {
			listener.exitPivotColumn(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitPivotColumn) {
			return visitor.visitPivotColumn(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class PivotValueContext extends ParserRuleContext {
	public expression(): ExpressionContext {
		return this.getRuleContext(0, ExpressionContext);
	}
	public identifier(): IdentifierContext | undefined {
		return this.tryGetRuleContext(0, IdentifierContext);
	}
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_pivotValue; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterPivotValue) {
			listener.enterPivotValue(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitPivotValue) {
			listener.exitPivotValue(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitPivotValue) {
			return visitor.visitPivotValue(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class LateralViewContext extends ParserRuleContext {
	public _tblName!: IdentifierContext;
	public _identifier!: IdentifierContext;
	public _colName: IdentifierContext[] = [];
	public LATERAL(): TerminalNode { return this.getToken(SparkSqlParser.LATERAL, 0); }
	public VIEW(): TerminalNode { return this.getToken(SparkSqlParser.VIEW, 0); }
	public qualifiedName(): QualifiedNameContext {
		return this.getRuleContext(0, QualifiedNameContext);
	}
	public identifier(): IdentifierContext[];
	public identifier(i: number): IdentifierContext;
	public identifier(i?: number): IdentifierContext | IdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(IdentifierContext);
		} else {
			return this.getRuleContext(i, IdentifierContext);
		}
	}
	public OUTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OUTER, 0); }
	public expression(): ExpressionContext[];
	public expression(i: number): ExpressionContext;
	public expression(i?: number): ExpressionContext | ExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ExpressionContext);
		} else {
			return this.getRuleContext(i, ExpressionContext);
		}
	}
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_lateralView; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterLateralView) {
			listener.enterLateralView(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitLateralView) {
			listener.exitLateralView(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitLateralView) {
			return visitor.visitLateralView(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class SetQuantifierContext extends ParserRuleContext {
	public DISTINCT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DISTINCT, 0); }
	public ALL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ALL, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_setQuantifier; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSetQuantifier) {
			listener.enterSetQuantifier(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSetQuantifier) {
			listener.exitSetQuantifier(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSetQuantifier) {
			return visitor.visitSetQuantifier(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class RelationContext extends ParserRuleContext {
	public relationPrimary(): RelationPrimaryContext {
		return this.getRuleContext(0, RelationPrimaryContext);
	}
	public joinRelation(): JoinRelationContext[];
	public joinRelation(i: number): JoinRelationContext;
	public joinRelation(i?: number): JoinRelationContext | JoinRelationContext[] {
		if (i === undefined) {
			return this.getRuleContexts(JoinRelationContext);
		} else {
			return this.getRuleContext(i, JoinRelationContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_relation; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterRelation) {
			listener.enterRelation(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitRelation) {
			listener.exitRelation(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitRelation) {
			return visitor.visitRelation(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class JoinRelationContext extends ParserRuleContext {
	public _right!: RelationPrimaryContext;
	public JOIN(): TerminalNode { return this.getToken(SparkSqlParser.JOIN, 0); }
	public relationPrimary(): RelationPrimaryContext {
		return this.getRuleContext(0, RelationPrimaryContext);
	}
	public joinType(): JoinTypeContext | undefined {
		return this.tryGetRuleContext(0, JoinTypeContext);
	}
	public joinCriteria(): JoinCriteriaContext | undefined {
		return this.tryGetRuleContext(0, JoinCriteriaContext);
	}
	public NATURAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NATURAL, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_joinRelation; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterJoinRelation) {
			listener.enterJoinRelation(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitJoinRelation) {
			listener.exitJoinRelation(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitJoinRelation) {
			return visitor.visitJoinRelation(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class JoinTypeContext extends ParserRuleContext {
	public INNER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INNER, 0); }
	public CROSS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CROSS, 0); }
	public LEFT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LEFT, 0); }
	public OUTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OUTER, 0); }
	public SEMI(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SEMI, 0); }
	public RIGHT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RIGHT, 0); }
	public FULL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FULL, 0); }
	public ANTI(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ANTI, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_joinType; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterJoinType) {
			listener.enterJoinType(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitJoinType) {
			listener.exitJoinType(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitJoinType) {
			return visitor.visitJoinType(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class JoinCriteriaContext extends ParserRuleContext {
	public ON(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ON, 0); }
	public booleanExpression(): BooleanExpressionContext | undefined {
		return this.tryGetRuleContext(0, BooleanExpressionContext);
	}
	public USING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.USING, 0); }
	public identifierList(): IdentifierListContext | undefined {
		return this.tryGetRuleContext(0, IdentifierListContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_joinCriteria; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterJoinCriteria) {
			listener.enterJoinCriteria(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitJoinCriteria) {
			listener.exitJoinCriteria(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitJoinCriteria) {
			return visitor.visitJoinCriteria(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class SampleContext extends ParserRuleContext {
	public TABLESAMPLE(): TerminalNode { return this.getToken(SparkSqlParser.TABLESAMPLE, 0); }
	public sampleMethod(): SampleMethodContext | undefined {
		return this.tryGetRuleContext(0, SampleMethodContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_sample; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSample) {
			listener.enterSample(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSample) {
			listener.exitSample(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSample) {
			return visitor.visitSample(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class SampleMethodContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_sampleMethod; }
	public copyFrom(ctx: SampleMethodContext): void {
		super.copyFrom(ctx);
	}
}
export class SampleByPercentileContext extends SampleMethodContext {
	public _negativeSign!: Token;
	public _percentage!: Token;
	public PERCENTLIT(): TerminalNode { return this.getToken(SparkSqlParser.PERCENTLIT, 0); }
	public INTEGER_VALUE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INTEGER_VALUE, 0); }
	public DECIMAL_VALUE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DECIMAL_VALUE, 0); }
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	constructor(ctx: SampleMethodContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSampleByPercentile) {
			listener.enterSampleByPercentile(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSampleByPercentile) {
			listener.exitSampleByPercentile(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSampleByPercentile) {
			return visitor.visitSampleByPercentile(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SampleByRowsContext extends SampleMethodContext {
	public expression(): ExpressionContext {
		return this.getRuleContext(0, ExpressionContext);
	}
	public ROWS(): TerminalNode { return this.getToken(SparkSqlParser.ROWS, 0); }
	constructor(ctx: SampleMethodContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSampleByRows) {
			listener.enterSampleByRows(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSampleByRows) {
			listener.exitSampleByRows(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSampleByRows) {
			return visitor.visitSampleByRows(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SampleByBucketContext extends SampleMethodContext {
	public _sampleType!: Token;
	public _numerator!: Token;
	public _denominator!: Token;
	public OUT(): TerminalNode { return this.getToken(SparkSqlParser.OUT, 0); }
	public OF(): TerminalNode { return this.getToken(SparkSqlParser.OF, 0); }
	public BUCKET(): TerminalNode { return this.getToken(SparkSqlParser.BUCKET, 0); }
	public INTEGER_VALUE(): TerminalNode[];
	public INTEGER_VALUE(i: number): TerminalNode;
	public INTEGER_VALUE(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.INTEGER_VALUE);
		} else {
			return this.getToken(SparkSqlParser.INTEGER_VALUE, i);
		}
	}
	public ON(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ON, 0); }
	public identifier(): IdentifierContext | undefined {
		return this.tryGetRuleContext(0, IdentifierContext);
	}
	public qualifiedName(): QualifiedNameContext | undefined {
		return this.tryGetRuleContext(0, QualifiedNameContext);
	}
	constructor(ctx: SampleMethodContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSampleByBucket) {
			listener.enterSampleByBucket(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSampleByBucket) {
			listener.exitSampleByBucket(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSampleByBucket) {
			return visitor.visitSampleByBucket(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SampleByBytesContext extends SampleMethodContext {
	public _bytes!: ExpressionContext;
	public expression(): ExpressionContext {
		return this.getRuleContext(0, ExpressionContext);
	}
	constructor(ctx: SampleMethodContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSampleByBytes) {
			listener.enterSampleByBytes(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSampleByBytes) {
			listener.exitSampleByBytes(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSampleByBytes) {
			return visitor.visitSampleByBytes(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class IdentifierListContext extends ParserRuleContext {
	public identifierSeq(): IdentifierSeqContext {
		return this.getRuleContext(0, IdentifierSeqContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_identifierList; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterIdentifierList) {
			listener.enterIdentifierList(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitIdentifierList) {
			listener.exitIdentifierList(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitIdentifierList) {
			return visitor.visitIdentifierList(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class IdentifierSeqContext extends ParserRuleContext {
	public _errorCapturingIdentifier!: ErrorCapturingIdentifierContext;
	public _ident: ErrorCapturingIdentifierContext[] = [];
	public errorCapturingIdentifier(): ErrorCapturingIdentifierContext[];
	public errorCapturingIdentifier(i: number): ErrorCapturingIdentifierContext;
	public errorCapturingIdentifier(i?: number): ErrorCapturingIdentifierContext | ErrorCapturingIdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ErrorCapturingIdentifierContext);
		} else {
			return this.getRuleContext(i, ErrorCapturingIdentifierContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_identifierSeq; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterIdentifierSeq) {
			listener.enterIdentifierSeq(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitIdentifierSeq) {
			listener.exitIdentifierSeq(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitIdentifierSeq) {
			return visitor.visitIdentifierSeq(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class OrderedIdentifierListContext extends ParserRuleContext {
	public orderedIdentifier(): OrderedIdentifierContext[];
	public orderedIdentifier(i: number): OrderedIdentifierContext;
	public orderedIdentifier(i?: number): OrderedIdentifierContext | OrderedIdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(OrderedIdentifierContext);
		} else {
			return this.getRuleContext(i, OrderedIdentifierContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_orderedIdentifierList; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterOrderedIdentifierList) {
			listener.enterOrderedIdentifierList(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitOrderedIdentifierList) {
			listener.exitOrderedIdentifierList(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitOrderedIdentifierList) {
			return visitor.visitOrderedIdentifierList(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class OrderedIdentifierContext extends ParserRuleContext {
	public _ident!: ErrorCapturingIdentifierContext;
	public _ordering!: Token;
	public errorCapturingIdentifier(): ErrorCapturingIdentifierContext {
		return this.getRuleContext(0, ErrorCapturingIdentifierContext);
	}
	public ASC(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ASC, 0); }
	public DESC(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DESC, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_orderedIdentifier; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterOrderedIdentifier) {
			listener.enterOrderedIdentifier(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitOrderedIdentifier) {
			listener.exitOrderedIdentifier(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitOrderedIdentifier) {
			return visitor.visitOrderedIdentifier(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class IdentifierCommentListContext extends ParserRuleContext {
	public identifierComment(): IdentifierCommentContext[];
	public identifierComment(i: number): IdentifierCommentContext;
	public identifierComment(i?: number): IdentifierCommentContext | IdentifierCommentContext[] {
		if (i === undefined) {
			return this.getRuleContexts(IdentifierCommentContext);
		} else {
			return this.getRuleContext(i, IdentifierCommentContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_identifierCommentList; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterIdentifierCommentList) {
			listener.enterIdentifierCommentList(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitIdentifierCommentList) {
			listener.exitIdentifierCommentList(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitIdentifierCommentList) {
			return visitor.visitIdentifierCommentList(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class IdentifierCommentContext extends ParserRuleContext {
	public identifier(): IdentifierContext {
		return this.getRuleContext(0, IdentifierContext);
	}
	public commentSpec(): CommentSpecContext | undefined {
		return this.tryGetRuleContext(0, CommentSpecContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_identifierComment; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterIdentifierComment) {
			listener.enterIdentifierComment(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitIdentifierComment) {
			listener.exitIdentifierComment(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitIdentifierComment) {
			return visitor.visitIdentifierComment(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class RelationPrimaryContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_relationPrimary; }
	public copyFrom(ctx: RelationPrimaryContext): void {
		super.copyFrom(ctx);
	}
}
export class TableNameContext extends RelationPrimaryContext {
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public tableAlias(): TableAliasContext {
		return this.getRuleContext(0, TableAliasContext);
	}
	public sample(): SampleContext | undefined {
		return this.tryGetRuleContext(0, SampleContext);
	}
	constructor(ctx: RelationPrimaryContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTableName) {
			listener.enterTableName(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTableName) {
			listener.exitTableName(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTableName) {
			return visitor.visitTableName(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class AliasedQueryContext extends RelationPrimaryContext {
	public query(): QueryContext {
		return this.getRuleContext(0, QueryContext);
	}
	public tableAlias(): TableAliasContext {
		return this.getRuleContext(0, TableAliasContext);
	}
	public sample(): SampleContext | undefined {
		return this.tryGetRuleContext(0, SampleContext);
	}
	constructor(ctx: RelationPrimaryContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterAliasedQuery) {
			listener.enterAliasedQuery(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitAliasedQuery) {
			listener.exitAliasedQuery(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitAliasedQuery) {
			return visitor.visitAliasedQuery(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class AliasedRelationContext extends RelationPrimaryContext {
	public relation(): RelationContext {
		return this.getRuleContext(0, RelationContext);
	}
	public tableAlias(): TableAliasContext {
		return this.getRuleContext(0, TableAliasContext);
	}
	public sample(): SampleContext | undefined {
		return this.tryGetRuleContext(0, SampleContext);
	}
	constructor(ctx: RelationPrimaryContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterAliasedRelation) {
			listener.enterAliasedRelation(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitAliasedRelation) {
			listener.exitAliasedRelation(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitAliasedRelation) {
			return visitor.visitAliasedRelation(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class InlineTableDefault2Context extends RelationPrimaryContext {
	public inlineTable(): InlineTableContext {
		return this.getRuleContext(0, InlineTableContext);
	}
	constructor(ctx: RelationPrimaryContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterInlineTableDefault2) {
			listener.enterInlineTableDefault2(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitInlineTableDefault2) {
			listener.exitInlineTableDefault2(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitInlineTableDefault2) {
			return visitor.visitInlineTableDefault2(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class TableValuedFunctionContext extends RelationPrimaryContext {
	public functionTable(): FunctionTableContext {
		return this.getRuleContext(0, FunctionTableContext);
	}
	constructor(ctx: RelationPrimaryContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTableValuedFunction) {
			listener.enterTableValuedFunction(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTableValuedFunction) {
			listener.exitTableValuedFunction(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTableValuedFunction) {
			return visitor.visitTableValuedFunction(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class InlineTableContext extends ParserRuleContext {
	public VALUES(): TerminalNode { return this.getToken(SparkSqlParser.VALUES, 0); }
	public expression(): ExpressionContext[];
	public expression(i: number): ExpressionContext;
	public expression(i?: number): ExpressionContext | ExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ExpressionContext);
		} else {
			return this.getRuleContext(i, ExpressionContext);
		}
	}
	public tableAlias(): TableAliasContext {
		return this.getRuleContext(0, TableAliasContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_inlineTable; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterInlineTable) {
			listener.enterInlineTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitInlineTable) {
			listener.exitInlineTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitInlineTable) {
			return visitor.visitInlineTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class FunctionTableContext extends ParserRuleContext {
	public _funcName!: ErrorCapturingIdentifierContext;
	public tableAlias(): TableAliasContext {
		return this.getRuleContext(0, TableAliasContext);
	}
	public errorCapturingIdentifier(): ErrorCapturingIdentifierContext {
		return this.getRuleContext(0, ErrorCapturingIdentifierContext);
	}
	public expression(): ExpressionContext[];
	public expression(i: number): ExpressionContext;
	public expression(i?: number): ExpressionContext | ExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ExpressionContext);
		} else {
			return this.getRuleContext(i, ExpressionContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_functionTable; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterFunctionTable) {
			listener.enterFunctionTable(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitFunctionTable) {
			listener.exitFunctionTable(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitFunctionTable) {
			return visitor.visitFunctionTable(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class TableAliasContext extends ParserRuleContext {
	public strictIdentifier(): StrictIdentifierContext | undefined {
		return this.tryGetRuleContext(0, StrictIdentifierContext);
	}
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	public identifierList(): IdentifierListContext | undefined {
		return this.tryGetRuleContext(0, IdentifierListContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_tableAlias; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTableAlias) {
			listener.enterTableAlias(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTableAlias) {
			listener.exitTableAlias(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTableAlias) {
			return visitor.visitTableAlias(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class RowFormatContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_rowFormat; }
	public copyFrom(ctx: RowFormatContext): void {
		super.copyFrom(ctx);
	}
}
export class RowFormatSerdeContext extends RowFormatContext {
	public _name!: Token;
	public _props!: TablePropertyListContext;
	public ROW(): TerminalNode { return this.getToken(SparkSqlParser.ROW, 0); }
	public FORMAT(): TerminalNode { return this.getToken(SparkSqlParser.FORMAT, 0); }
	public SERDE(): TerminalNode { return this.getToken(SparkSqlParser.SERDE, 0); }
	public STRING(): TerminalNode { return this.getToken(SparkSqlParser.STRING, 0); }
	public WITH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.WITH, 0); }
	public SERDEPROPERTIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SERDEPROPERTIES, 0); }
	public tablePropertyList(): TablePropertyListContext | undefined {
		return this.tryGetRuleContext(0, TablePropertyListContext);
	}
	constructor(ctx: RowFormatContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterRowFormatSerde) {
			listener.enterRowFormatSerde(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitRowFormatSerde) {
			listener.exitRowFormatSerde(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitRowFormatSerde) {
			return visitor.visitRowFormatSerde(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class RowFormatDelimitedContext extends RowFormatContext {
	public _fieldsTerminatedBy!: Token;
	public _escapedBy!: Token;
	public _collectionItemsTerminatedBy!: Token;
	public _keysTerminatedBy!: Token;
	public _linesSeparatedBy!: Token;
	public _nullDefinedAs!: Token;
	public ROW(): TerminalNode { return this.getToken(SparkSqlParser.ROW, 0); }
	public FORMAT(): TerminalNode { return this.getToken(SparkSqlParser.FORMAT, 0); }
	public DELIMITED(): TerminalNode { return this.getToken(SparkSqlParser.DELIMITED, 0); }
	public FIELDS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FIELDS, 0); }
	public TERMINATED(): TerminalNode[];
	public TERMINATED(i: number): TerminalNode;
	public TERMINATED(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.TERMINATED);
		} else {
			return this.getToken(SparkSqlParser.TERMINATED, i);
		}
	}
	public BY(): TerminalNode[];
	public BY(i: number): TerminalNode;
	public BY(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.BY);
		} else {
			return this.getToken(SparkSqlParser.BY, i);
		}
	}
	public COLLECTION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLLECTION, 0); }
	public ITEMS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ITEMS, 0); }
	public MAP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MAP, 0); }
	public KEYS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.KEYS, 0); }
	public LINES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LINES, 0); }
	public NULL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NULL, 0); }
	public DEFINED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DEFINED, 0); }
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	public STRING(): TerminalNode[];
	public STRING(i: number): TerminalNode;
	public STRING(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.STRING);
		} else {
			return this.getToken(SparkSqlParser.STRING, i);
		}
	}
	public ESCAPED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ESCAPED, 0); }
	constructor(ctx: RowFormatContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterRowFormatDelimited) {
			listener.enterRowFormatDelimited(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitRowFormatDelimited) {
			listener.exitRowFormatDelimited(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitRowFormatDelimited) {
			return visitor.visitRowFormatDelimited(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class MultipartIdentifierListContext extends ParserRuleContext {
	public multipartIdentifier(): MultipartIdentifierContext[];
	public multipartIdentifier(i: number): MultipartIdentifierContext;
	public multipartIdentifier(i?: number): MultipartIdentifierContext | MultipartIdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(MultipartIdentifierContext);
		} else {
			return this.getRuleContext(i, MultipartIdentifierContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_multipartIdentifierList; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterMultipartIdentifierList) {
			listener.enterMultipartIdentifierList(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitMultipartIdentifierList) {
			listener.exitMultipartIdentifierList(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitMultipartIdentifierList) {
			return visitor.visitMultipartIdentifierList(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class MultipartIdentifierContext extends ParserRuleContext {
	public _errorCapturingIdentifier!: ErrorCapturingIdentifierContext;
	public _parts: ErrorCapturingIdentifierContext[] = [];
	public errorCapturingIdentifier(): ErrorCapturingIdentifierContext[];
	public errorCapturingIdentifier(i: number): ErrorCapturingIdentifierContext;
	public errorCapturingIdentifier(i?: number): ErrorCapturingIdentifierContext | ErrorCapturingIdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ErrorCapturingIdentifierContext);
		} else {
			return this.getRuleContext(i, ErrorCapturingIdentifierContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_multipartIdentifier; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterMultipartIdentifier) {
			listener.enterMultipartIdentifier(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitMultipartIdentifier) {
			listener.exitMultipartIdentifier(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitMultipartIdentifier) {
			return visitor.visitMultipartIdentifier(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class TableIdentifierContext extends ParserRuleContext {
	public _db!: ErrorCapturingIdentifierContext;
	public _table!: ErrorCapturingIdentifierContext;
	public errorCapturingIdentifier(): ErrorCapturingIdentifierContext[];
	public errorCapturingIdentifier(i: number): ErrorCapturingIdentifierContext;
	public errorCapturingIdentifier(i?: number): ErrorCapturingIdentifierContext | ErrorCapturingIdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ErrorCapturingIdentifierContext);
		} else {
			return this.getRuleContext(i, ErrorCapturingIdentifierContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_tableIdentifier; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTableIdentifier) {
			listener.enterTableIdentifier(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTableIdentifier) {
			listener.exitTableIdentifier(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTableIdentifier) {
			return visitor.visitTableIdentifier(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class NamedExpressionContext extends ParserRuleContext {
	public _name!: ErrorCapturingIdentifierContext;
	public expression(): ExpressionContext {
		return this.getRuleContext(0, ExpressionContext);
	}
	public identifierList(): IdentifierListContext | undefined {
		return this.tryGetRuleContext(0, IdentifierListContext);
	}
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	public errorCapturingIdentifier(): ErrorCapturingIdentifierContext | undefined {
		return this.tryGetRuleContext(0, ErrorCapturingIdentifierContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_namedExpression; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterNamedExpression) {
			listener.enterNamedExpression(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitNamedExpression) {
			listener.exitNamedExpression(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitNamedExpression) {
			return visitor.visitNamedExpression(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class NamedExpressionSeqContext extends ParserRuleContext {
	public namedExpression(): NamedExpressionContext[];
	public namedExpression(i: number): NamedExpressionContext;
	public namedExpression(i?: number): NamedExpressionContext | NamedExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(NamedExpressionContext);
		} else {
			return this.getRuleContext(i, NamedExpressionContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_namedExpressionSeq; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterNamedExpressionSeq) {
			listener.enterNamedExpressionSeq(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitNamedExpressionSeq) {
			listener.exitNamedExpressionSeq(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitNamedExpressionSeq) {
			return visitor.visitNamedExpressionSeq(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class TransformListContext extends ParserRuleContext {
	public _transform!: TransformContext;
	public _transforms: TransformContext[] = [];
	public transform(): TransformContext[];
	public transform(i: number): TransformContext;
	public transform(i?: number): TransformContext | TransformContext[] {
		if (i === undefined) {
			return this.getRuleContexts(TransformContext);
		} else {
			return this.getRuleContext(i, TransformContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_transformList; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTransformList) {
			listener.enterTransformList(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTransformList) {
			listener.exitTransformList(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTransformList) {
			return visitor.visitTransformList(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class TransformContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_transform; }
	public copyFrom(ctx: TransformContext): void {
		super.copyFrom(ctx);
	}
}
export class IdentityTransformContext extends TransformContext {
	public qualifiedName(): QualifiedNameContext {
		return this.getRuleContext(0, QualifiedNameContext);
	}
	constructor(ctx: TransformContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterIdentityTransform) {
			listener.enterIdentityTransform(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitIdentityTransform) {
			listener.exitIdentityTransform(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitIdentityTransform) {
			return visitor.visitIdentityTransform(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ApplyTransformContext extends TransformContext {
	public _transformName!: IdentifierContext;
	public _transformArgument!: TransformArgumentContext;
	public _argument: TransformArgumentContext[] = [];
	public identifier(): IdentifierContext {
		return this.getRuleContext(0, IdentifierContext);
	}
	public transformArgument(): TransformArgumentContext[];
	public transformArgument(i: number): TransformArgumentContext;
	public transformArgument(i?: number): TransformArgumentContext | TransformArgumentContext[] {
		if (i === undefined) {
			return this.getRuleContexts(TransformArgumentContext);
		} else {
			return this.getRuleContext(i, TransformArgumentContext);
		}
	}
	constructor(ctx: TransformContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterApplyTransform) {
			listener.enterApplyTransform(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitApplyTransform) {
			listener.exitApplyTransform(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitApplyTransform) {
			return visitor.visitApplyTransform(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class TransformArgumentContext extends ParserRuleContext {
	public qualifiedName(): QualifiedNameContext | undefined {
		return this.tryGetRuleContext(0, QualifiedNameContext);
	}
	public constant(): ConstantContext | undefined {
		return this.tryGetRuleContext(0, ConstantContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_transformArgument; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTransformArgument) {
			listener.enterTransformArgument(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTransformArgument) {
			listener.exitTransformArgument(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTransformArgument) {
			return visitor.visitTransformArgument(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ExpressionContext extends ParserRuleContext {
	public booleanExpression(): BooleanExpressionContext {
		return this.getRuleContext(0, BooleanExpressionContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_expression; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterExpression) {
			listener.enterExpression(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitExpression) {
			listener.exitExpression(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitExpression) {
			return visitor.visitExpression(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class BooleanExpressionContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_booleanExpression; }
	public copyFrom(ctx: BooleanExpressionContext): void {
		super.copyFrom(ctx);
	}
}
export class LogicalNotContext extends BooleanExpressionContext {
	public NOT(): TerminalNode { return this.getToken(SparkSqlParser.NOT, 0); }
	public booleanExpression(): BooleanExpressionContext {
		return this.getRuleContext(0, BooleanExpressionContext);
	}
	constructor(ctx: BooleanExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterLogicalNot) {
			listener.enterLogicalNot(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitLogicalNot) {
			listener.exitLogicalNot(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitLogicalNot) {
			return visitor.visitLogicalNot(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ExistsContext extends BooleanExpressionContext {
	public EXISTS(): TerminalNode { return this.getToken(SparkSqlParser.EXISTS, 0); }
	public query(): QueryContext {
		return this.getRuleContext(0, QueryContext);
	}
	constructor(ctx: BooleanExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterExists) {
			listener.enterExists(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitExists) {
			listener.exitExists(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitExists) {
			return visitor.visitExists(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class PredicatedContext extends BooleanExpressionContext {
	public valueExpression(): ValueExpressionContext {
		return this.getRuleContext(0, ValueExpressionContext);
	}
	public predicate(): PredicateContext | undefined {
		return this.tryGetRuleContext(0, PredicateContext);
	}
	constructor(ctx: BooleanExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterPredicated) {
			listener.enterPredicated(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitPredicated) {
			listener.exitPredicated(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitPredicated) {
			return visitor.visitPredicated(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class LogicalBinaryContext extends BooleanExpressionContext {
	public _left!: BooleanExpressionContext;
	public _operator!: Token;
	public _right!: BooleanExpressionContext;
	public booleanExpression(): BooleanExpressionContext[];
	public booleanExpression(i: number): BooleanExpressionContext;
	public booleanExpression(i?: number): BooleanExpressionContext | BooleanExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(BooleanExpressionContext);
		} else {
			return this.getRuleContext(i, BooleanExpressionContext);
		}
	}
	public AND(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AND, 0); }
	public OR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OR, 0); }
	constructor(ctx: BooleanExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterLogicalBinary) {
			listener.enterLogicalBinary(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitLogicalBinary) {
			listener.exitLogicalBinary(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitLogicalBinary) {
			return visitor.visitLogicalBinary(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class PredicateContext extends ParserRuleContext {
	public _kind!: Token;
	public _lower!: ValueExpressionContext;
	public _upper!: ValueExpressionContext;
	public _pattern!: ValueExpressionContext;
	public _quantifier!: Token;
	public _escapeChar!: Token;
	public _right!: ValueExpressionContext;
	public AND(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AND, 0); }
	public BETWEEN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.BETWEEN, 0); }
	public valueExpression(): ValueExpressionContext[];
	public valueExpression(i: number): ValueExpressionContext;
	public valueExpression(i?: number): ValueExpressionContext | ValueExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ValueExpressionContext);
		} else {
			return this.getRuleContext(i, ValueExpressionContext);
		}
	}
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public expression(): ExpressionContext[];
	public expression(i: number): ExpressionContext;
	public expression(i?: number): ExpressionContext | ExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ExpressionContext);
		} else {
			return this.getRuleContext(i, ExpressionContext);
		}
	}
	public IN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IN, 0); }
	public query(): QueryContext | undefined {
		return this.tryGetRuleContext(0, QueryContext);
	}
	public RLIKE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RLIKE, 0); }
	public LIKE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LIKE, 0); }
	public ANY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ANY, 0); }
	public SOME(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SOME, 0); }
	public ALL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ALL, 0); }
	public ESCAPE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ESCAPE, 0); }
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	public IS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IS, 0); }
	public NULL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NULL, 0); }
	public TRUE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRUE, 0); }
	public FALSE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FALSE, 0); }
	public UNKNOWN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNKNOWN, 0); }
	public FROM(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FROM, 0); }
	public DISTINCT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DISTINCT, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_predicate; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterPredicate) {
			listener.enterPredicate(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitPredicate) {
			listener.exitPredicate(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitPredicate) {
			return visitor.visitPredicate(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ValueExpressionContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_valueExpression; }
	public copyFrom(ctx: ValueExpressionContext): void {
		super.copyFrom(ctx);
	}
}
export class ValueExpressionDefaultContext extends ValueExpressionContext {
	public primaryExpression(): PrimaryExpressionContext {
		return this.getRuleContext(0, PrimaryExpressionContext);
	}
	constructor(ctx: ValueExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterValueExpressionDefault) {
			listener.enterValueExpressionDefault(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitValueExpressionDefault) {
			listener.exitValueExpressionDefault(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitValueExpressionDefault) {
			return visitor.visitValueExpressionDefault(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ArithmeticUnaryContext extends ValueExpressionContext {
	public _operator!: Token;
	public valueExpression(): ValueExpressionContext {
		return this.getRuleContext(0, ValueExpressionContext);
	}
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	public PLUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PLUS, 0); }
	public TILDE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TILDE, 0); }
	constructor(ctx: ValueExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterArithmeticUnary) {
			listener.enterArithmeticUnary(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitArithmeticUnary) {
			listener.exitArithmeticUnary(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitArithmeticUnary) {
			return visitor.visitArithmeticUnary(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ArithmeticBinaryContext extends ValueExpressionContext {
	public _left!: ValueExpressionContext;
	public _operator!: Token;
	public _right!: ValueExpressionContext;
	public valueExpression(): ValueExpressionContext[];
	public valueExpression(i: number): ValueExpressionContext;
	public valueExpression(i?: number): ValueExpressionContext | ValueExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ValueExpressionContext);
		} else {
			return this.getRuleContext(i, ValueExpressionContext);
		}
	}
	public ASTERISK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ASTERISK, 0); }
	public SLASH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SLASH, 0); }
	public PERCENT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PERCENT, 0); }
	public DIV(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DIV, 0); }
	public PLUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PLUS, 0); }
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	public CONCAT_PIPE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CONCAT_PIPE, 0); }
	public AMPERSAND(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AMPERSAND, 0); }
	public HAT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.HAT, 0); }
	public PIPE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PIPE, 0); }
	constructor(ctx: ValueExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterArithmeticBinary) {
			listener.enterArithmeticBinary(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitArithmeticBinary) {
			listener.exitArithmeticBinary(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitArithmeticBinary) {
			return visitor.visitArithmeticBinary(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ComparisonContext extends ValueExpressionContext {
	public _left!: ValueExpressionContext;
	public _right!: ValueExpressionContext;
	public comparisonOperator(): ComparisonOperatorContext {
		return this.getRuleContext(0, ComparisonOperatorContext);
	}
	public valueExpression(): ValueExpressionContext[];
	public valueExpression(i: number): ValueExpressionContext;
	public valueExpression(i?: number): ValueExpressionContext | ValueExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ValueExpressionContext);
		} else {
			return this.getRuleContext(i, ValueExpressionContext);
		}
	}
	constructor(ctx: ValueExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterComparison) {
			listener.enterComparison(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitComparison) {
			listener.exitComparison(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitComparison) {
			return visitor.visitComparison(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class PrimaryExpressionContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_primaryExpression; }
	public copyFrom(ctx: PrimaryExpressionContext): void {
		super.copyFrom(ctx);
	}
}
export class CurrentDatetimeContext extends PrimaryExpressionContext {
	public _name!: Token;
	public CURRENT_DATE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CURRENT_DATE, 0); }
	public CURRENT_TIMESTAMP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CURRENT_TIMESTAMP, 0); }
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCurrentDatetime) {
			listener.enterCurrentDatetime(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCurrentDatetime) {
			listener.exitCurrentDatetime(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCurrentDatetime) {
			return visitor.visitCurrentDatetime(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SearchedCaseContext extends PrimaryExpressionContext {
	public _elseExpression!: ExpressionContext;
	public CASE(): TerminalNode { return this.getToken(SparkSqlParser.CASE, 0); }
	public END(): TerminalNode { return this.getToken(SparkSqlParser.END, 0); }
	public whenClause(): WhenClauseContext[];
	public whenClause(i: number): WhenClauseContext;
	public whenClause(i?: number): WhenClauseContext | WhenClauseContext[] {
		if (i === undefined) {
			return this.getRuleContexts(WhenClauseContext);
		} else {
			return this.getRuleContext(i, WhenClauseContext);
		}
	}
	public ELSE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ELSE, 0); }
	public expression(): ExpressionContext | undefined {
		return this.tryGetRuleContext(0, ExpressionContext);
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSearchedCase) {
			listener.enterSearchedCase(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSearchedCase) {
			listener.exitSearchedCase(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSearchedCase) {
			return visitor.visitSearchedCase(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SimpleCaseContext extends PrimaryExpressionContext {
	public _value!: ExpressionContext;
	public _elseExpression!: ExpressionContext;
	public CASE(): TerminalNode { return this.getToken(SparkSqlParser.CASE, 0); }
	public END(): TerminalNode { return this.getToken(SparkSqlParser.END, 0); }
	public expression(): ExpressionContext[];
	public expression(i: number): ExpressionContext;
	public expression(i?: number): ExpressionContext | ExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ExpressionContext);
		} else {
			return this.getRuleContext(i, ExpressionContext);
		}
	}
	public whenClause(): WhenClauseContext[];
	public whenClause(i: number): WhenClauseContext;
	public whenClause(i?: number): WhenClauseContext | WhenClauseContext[] {
		if (i === undefined) {
			return this.getRuleContexts(WhenClauseContext);
		} else {
			return this.getRuleContext(i, WhenClauseContext);
		}
	}
	public ELSE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ELSE, 0); }
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSimpleCase) {
			listener.enterSimpleCase(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSimpleCase) {
			listener.exitSimpleCase(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSimpleCase) {
			return visitor.visitSimpleCase(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class CastContext extends PrimaryExpressionContext {
	public CAST(): TerminalNode { return this.getToken(SparkSqlParser.CAST, 0); }
	public expression(): ExpressionContext {
		return this.getRuleContext(0, ExpressionContext);
	}
	public AS(): TerminalNode { return this.getToken(SparkSqlParser.AS, 0); }
	public dataType(): DataTypeContext {
		return this.getRuleContext(0, DataTypeContext);
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterCast) {
			listener.enterCast(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitCast) {
			listener.exitCast(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitCast) {
			return visitor.visitCast(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class StructContext extends PrimaryExpressionContext {
	public _namedExpression!: NamedExpressionContext;
	public _argument: NamedExpressionContext[] = [];
	public STRUCT(): TerminalNode { return this.getToken(SparkSqlParser.STRUCT, 0); }
	public namedExpression(): NamedExpressionContext[];
	public namedExpression(i: number): NamedExpressionContext;
	public namedExpression(i?: number): NamedExpressionContext | NamedExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(NamedExpressionContext);
		} else {
			return this.getRuleContext(i, NamedExpressionContext);
		}
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterStruct) {
			listener.enterStruct(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitStruct) {
			listener.exitStruct(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitStruct) {
			return visitor.visitStruct(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class FirstContext extends PrimaryExpressionContext {
	public FIRST(): TerminalNode { return this.getToken(SparkSqlParser.FIRST, 0); }
	public expression(): ExpressionContext {
		return this.getRuleContext(0, ExpressionContext);
	}
	public IGNORE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IGNORE, 0); }
	public NULLS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NULLS, 0); }
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterFirst) {
			listener.enterFirst(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitFirst) {
			listener.exitFirst(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitFirst) {
			return visitor.visitFirst(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class LastContext extends PrimaryExpressionContext {
	public LAST(): TerminalNode { return this.getToken(SparkSqlParser.LAST, 0); }
	public expression(): ExpressionContext {
		return this.getRuleContext(0, ExpressionContext);
	}
	public IGNORE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IGNORE, 0); }
	public NULLS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NULLS, 0); }
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterLast) {
			listener.enterLast(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitLast) {
			listener.exitLast(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitLast) {
			return visitor.visitLast(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class PositionContext extends PrimaryExpressionContext {
	public _substr!: ValueExpressionContext;
	public _str!: ValueExpressionContext;
	public POSITION(): TerminalNode { return this.getToken(SparkSqlParser.POSITION, 0); }
	public IN(): TerminalNode { return this.getToken(SparkSqlParser.IN, 0); }
	public valueExpression(): ValueExpressionContext[];
	public valueExpression(i: number): ValueExpressionContext;
	public valueExpression(i?: number): ValueExpressionContext | ValueExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ValueExpressionContext);
		} else {
			return this.getRuleContext(i, ValueExpressionContext);
		}
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterPosition) {
			listener.enterPosition(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitPosition) {
			listener.exitPosition(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitPosition) {
			return visitor.visitPosition(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ConstantDefaultContext extends PrimaryExpressionContext {
	public constant(): ConstantContext {
		return this.getRuleContext(0, ConstantContext);
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterConstantDefault) {
			listener.enterConstantDefault(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitConstantDefault) {
			listener.exitConstantDefault(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitConstantDefault) {
			return visitor.visitConstantDefault(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class StarContext extends PrimaryExpressionContext {
	public ASTERISK(): TerminalNode { return this.getToken(SparkSqlParser.ASTERISK, 0); }
	public qualifiedName(): QualifiedNameContext | undefined {
		return this.tryGetRuleContext(0, QualifiedNameContext);
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterStar) {
			listener.enterStar(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitStar) {
			listener.exitStar(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitStar) {
			return visitor.visitStar(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class RowConstructorContext extends PrimaryExpressionContext {
	public namedExpression(): NamedExpressionContext[];
	public namedExpression(i: number): NamedExpressionContext;
	public namedExpression(i?: number): NamedExpressionContext | NamedExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(NamedExpressionContext);
		} else {
			return this.getRuleContext(i, NamedExpressionContext);
		}
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterRowConstructor) {
			listener.enterRowConstructor(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitRowConstructor) {
			listener.exitRowConstructor(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitRowConstructor) {
			return visitor.visitRowConstructor(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SubqueryExpressionContext extends PrimaryExpressionContext {
	public query(): QueryContext {
		return this.getRuleContext(0, QueryContext);
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSubqueryExpression) {
			listener.enterSubqueryExpression(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSubqueryExpression) {
			listener.exitSubqueryExpression(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSubqueryExpression) {
			return visitor.visitSubqueryExpression(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class FunctionCallContext extends PrimaryExpressionContext {
	public _expression!: ExpressionContext;
	public _argument: ExpressionContext[] = [];
	public _where!: BooleanExpressionContext;
	public functionName(): FunctionNameContext {
		return this.getRuleContext(0, FunctionNameContext);
	}
	public FILTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FILTER, 0); }
	public WHERE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.WHERE, 0); }
	public OVER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OVER, 0); }
	public windowSpec(): WindowSpecContext | undefined {
		return this.tryGetRuleContext(0, WindowSpecContext);
	}
	public expression(): ExpressionContext[];
	public expression(i: number): ExpressionContext;
	public expression(i?: number): ExpressionContext | ExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ExpressionContext);
		} else {
			return this.getRuleContext(i, ExpressionContext);
		}
	}
	public booleanExpression(): BooleanExpressionContext | undefined {
		return this.tryGetRuleContext(0, BooleanExpressionContext);
	}
	public setQuantifier(): SetQuantifierContext | undefined {
		return this.tryGetRuleContext(0, SetQuantifierContext);
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterFunctionCall) {
			listener.enterFunctionCall(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitFunctionCall) {
			listener.exitFunctionCall(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitFunctionCall) {
			return visitor.visitFunctionCall(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class LambdaContext extends PrimaryExpressionContext {
	public identifier(): IdentifierContext[];
	public identifier(i: number): IdentifierContext;
	public identifier(i?: number): IdentifierContext | IdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(IdentifierContext);
		} else {
			return this.getRuleContext(i, IdentifierContext);
		}
	}
	public expression(): ExpressionContext {
		return this.getRuleContext(0, ExpressionContext);
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterLambda) {
			listener.enterLambda(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitLambda) {
			listener.exitLambda(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitLambda) {
			return visitor.visitLambda(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SubscriptContext extends PrimaryExpressionContext {
	public _value!: PrimaryExpressionContext;
	public _index!: ValueExpressionContext;
	public primaryExpression(): PrimaryExpressionContext {
		return this.getRuleContext(0, PrimaryExpressionContext);
	}
	public valueExpression(): ValueExpressionContext {
		return this.getRuleContext(0, ValueExpressionContext);
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSubscript) {
			listener.enterSubscript(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSubscript) {
			listener.exitSubscript(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSubscript) {
			return visitor.visitSubscript(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ColumnReferenceContext extends PrimaryExpressionContext {
	public identifier(): IdentifierContext {
		return this.getRuleContext(0, IdentifierContext);
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterColumnReference) {
			listener.enterColumnReference(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitColumnReference) {
			listener.exitColumnReference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitColumnReference) {
			return visitor.visitColumnReference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DereferenceContext extends PrimaryExpressionContext {
	public _base!: PrimaryExpressionContext;
	public _fieldName!: IdentifierContext;
	public primaryExpression(): PrimaryExpressionContext {
		return this.getRuleContext(0, PrimaryExpressionContext);
	}
	public identifier(): IdentifierContext {
		return this.getRuleContext(0, IdentifierContext);
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDereference) {
			listener.enterDereference(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDereference) {
			listener.exitDereference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDereference) {
			return visitor.visitDereference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ParenthesizedExpressionContext extends PrimaryExpressionContext {
	public expression(): ExpressionContext {
		return this.getRuleContext(0, ExpressionContext);
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterParenthesizedExpression) {
			listener.enterParenthesizedExpression(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitParenthesizedExpression) {
			listener.exitParenthesizedExpression(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitParenthesizedExpression) {
			return visitor.visitParenthesizedExpression(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class ExtractContext extends PrimaryExpressionContext {
	public _field!: IdentifierContext;
	public _source!: ValueExpressionContext;
	public EXTRACT(): TerminalNode { return this.getToken(SparkSqlParser.EXTRACT, 0); }
	public FROM(): TerminalNode { return this.getToken(SparkSqlParser.FROM, 0); }
	public identifier(): IdentifierContext {
		return this.getRuleContext(0, IdentifierContext);
	}
	public valueExpression(): ValueExpressionContext {
		return this.getRuleContext(0, ValueExpressionContext);
	}
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterExtract) {
			listener.enterExtract(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitExtract) {
			listener.exitExtract(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitExtract) {
			return visitor.visitExtract(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SubstringContext extends PrimaryExpressionContext {
	public _str!: ValueExpressionContext;
	public _pos!: ValueExpressionContext;
	public _len!: ValueExpressionContext;
	public SUBSTR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SUBSTR, 0); }
	public SUBSTRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SUBSTRING, 0); }
	public valueExpression(): ValueExpressionContext[];
	public valueExpression(i: number): ValueExpressionContext;
	public valueExpression(i?: number): ValueExpressionContext | ValueExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ValueExpressionContext);
		} else {
			return this.getRuleContext(i, ValueExpressionContext);
		}
	}
	public FROM(): TerminalNode { return this.getToken(SparkSqlParser.FROM, 0); }
	public FOR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FOR, 0); }
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSubstring) {
			listener.enterSubstring(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSubstring) {
			listener.exitSubstring(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSubstring) {
			return visitor.visitSubstring(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class TrimContext extends PrimaryExpressionContext {
	public _trimOption!: Token;
	public _trimStr!: ValueExpressionContext;
	public _srcStr!: ValueExpressionContext;
	public TRIM(): TerminalNode { return this.getToken(SparkSqlParser.TRIM, 0); }
	public FROM(): TerminalNode { return this.getToken(SparkSqlParser.FROM, 0); }
	public valueExpression(): ValueExpressionContext[];
	public valueExpression(i: number): ValueExpressionContext;
	public valueExpression(i?: number): ValueExpressionContext | ValueExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ValueExpressionContext);
		} else {
			return this.getRuleContext(i, ValueExpressionContext);
		}
	}
	public BOTH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.BOTH, 0); }
	public LEADING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LEADING, 0); }
	public TRAILING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRAILING, 0); }
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTrim) {
			listener.enterTrim(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTrim) {
			listener.exitTrim(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTrim) {
			return visitor.visitTrim(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class OverlayContext extends PrimaryExpressionContext {
	public _input!: ValueExpressionContext;
	public _replace!: ValueExpressionContext;
	public _position!: ValueExpressionContext;
	public _length!: ValueExpressionContext;
	public OVERLAY(): TerminalNode { return this.getToken(SparkSqlParser.OVERLAY, 0); }
	public PLACING(): TerminalNode { return this.getToken(SparkSqlParser.PLACING, 0); }
	public FROM(): TerminalNode { return this.getToken(SparkSqlParser.FROM, 0); }
	public valueExpression(): ValueExpressionContext[];
	public valueExpression(i: number): ValueExpressionContext;
	public valueExpression(i?: number): ValueExpressionContext | ValueExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ValueExpressionContext);
		} else {
			return this.getRuleContext(i, ValueExpressionContext);
		}
	}
	public FOR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FOR, 0); }
	constructor(ctx: PrimaryExpressionContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterOverlay) {
			listener.enterOverlay(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitOverlay) {
			listener.exitOverlay(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitOverlay) {
			return visitor.visitOverlay(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ConstantContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_constant; }
	public copyFrom(ctx: ConstantContext): void {
		super.copyFrom(ctx);
	}
}
export class NullLiteralContext extends ConstantContext {
	public NULL(): TerminalNode { return this.getToken(SparkSqlParser.NULL, 0); }
	constructor(ctx: ConstantContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterNullLiteral) {
			listener.enterNullLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitNullLiteral) {
			listener.exitNullLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitNullLiteral) {
			return visitor.visitNullLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class IntervalLiteralContext extends ConstantContext {
	public interval(): IntervalContext {
		return this.getRuleContext(0, IntervalContext);
	}
	constructor(ctx: ConstantContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterIntervalLiteral) {
			listener.enterIntervalLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitIntervalLiteral) {
			listener.exitIntervalLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitIntervalLiteral) {
			return visitor.visitIntervalLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class TypeConstructorContext extends ConstantContext {
	public identifier(): IdentifierContext {
		return this.getRuleContext(0, IdentifierContext);
	}
	public STRING(): TerminalNode { return this.getToken(SparkSqlParser.STRING, 0); }
	constructor(ctx: ConstantContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTypeConstructor) {
			listener.enterTypeConstructor(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTypeConstructor) {
			listener.exitTypeConstructor(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTypeConstructor) {
			return visitor.visitTypeConstructor(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class NumericLiteralContext extends ConstantContext {
	public number(): NumberContext {
		return this.getRuleContext(0, NumberContext);
	}
	constructor(ctx: ConstantContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterNumericLiteral) {
			listener.enterNumericLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitNumericLiteral) {
			listener.exitNumericLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitNumericLiteral) {
			return visitor.visitNumericLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class BooleanLiteralContext extends ConstantContext {
	public booleanValue(): BooleanValueContext {
		return this.getRuleContext(0, BooleanValueContext);
	}
	constructor(ctx: ConstantContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterBooleanLiteral) {
			listener.enterBooleanLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitBooleanLiteral) {
			listener.exitBooleanLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitBooleanLiteral) {
			return visitor.visitBooleanLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class StringLiteralContext extends ConstantContext {
	public STRING(): TerminalNode[];
	public STRING(i: number): TerminalNode;
	public STRING(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.STRING);
		} else {
			return this.getToken(SparkSqlParser.STRING, i);
		}
	}
	constructor(ctx: ConstantContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterStringLiteral) {
			listener.enterStringLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitStringLiteral) {
			listener.exitStringLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitStringLiteral) {
			return visitor.visitStringLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ComparisonOperatorContext extends ParserRuleContext {
	public EQ(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EQ, 0); }
	public NEQ(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NEQ, 0); }
	public NEQJ(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NEQJ, 0); }
	public LT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LT, 0); }
	public LTE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LTE, 0); }
	public GT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.GT, 0); }
	public GTE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.GTE, 0); }
	public NSEQ(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NSEQ, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_comparisonOperator; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterComparisonOperator) {
			listener.enterComparisonOperator(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitComparisonOperator) {
			listener.exitComparisonOperator(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitComparisonOperator) {
			return visitor.visitComparisonOperator(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ArithmeticOperatorContext extends ParserRuleContext {
	public PLUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PLUS, 0); }
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	public ASTERISK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ASTERISK, 0); }
	public SLASH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SLASH, 0); }
	public PERCENT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PERCENT, 0); }
	public DIV(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DIV, 0); }
	public TILDE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TILDE, 0); }
	public AMPERSAND(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AMPERSAND, 0); }
	public PIPE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PIPE, 0); }
	public CONCAT_PIPE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CONCAT_PIPE, 0); }
	public HAT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.HAT, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_arithmeticOperator; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterArithmeticOperator) {
			listener.enterArithmeticOperator(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitArithmeticOperator) {
			listener.exitArithmeticOperator(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitArithmeticOperator) {
			return visitor.visitArithmeticOperator(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class PredicateOperatorContext extends ParserRuleContext {
	public OR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OR, 0); }
	public AND(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AND, 0); }
	public IN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IN, 0); }
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_predicateOperator; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterPredicateOperator) {
			listener.enterPredicateOperator(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitPredicateOperator) {
			listener.exitPredicateOperator(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitPredicateOperator) {
			return visitor.visitPredicateOperator(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class BooleanValueContext extends ParserRuleContext {
	public TRUE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRUE, 0); }
	public FALSE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FALSE, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_booleanValue; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterBooleanValue) {
			listener.enterBooleanValue(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitBooleanValue) {
			listener.exitBooleanValue(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitBooleanValue) {
			return visitor.visitBooleanValue(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class IntervalContext extends ParserRuleContext {
	public INTERVAL(): TerminalNode { return this.getToken(SparkSqlParser.INTERVAL, 0); }
	public errorCapturingMultiUnitsInterval(): ErrorCapturingMultiUnitsIntervalContext | undefined {
		return this.tryGetRuleContext(0, ErrorCapturingMultiUnitsIntervalContext);
	}
	public errorCapturingUnitToUnitInterval(): ErrorCapturingUnitToUnitIntervalContext | undefined {
		return this.tryGetRuleContext(0, ErrorCapturingUnitToUnitIntervalContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_interval; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterInterval) {
			listener.enterInterval(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitInterval) {
			listener.exitInterval(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitInterval) {
			return visitor.visitInterval(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ErrorCapturingMultiUnitsIntervalContext extends ParserRuleContext {
	public multiUnitsInterval(): MultiUnitsIntervalContext {
		return this.getRuleContext(0, MultiUnitsIntervalContext);
	}
	public unitToUnitInterval(): UnitToUnitIntervalContext | undefined {
		return this.tryGetRuleContext(0, UnitToUnitIntervalContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_errorCapturingMultiUnitsInterval; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterErrorCapturingMultiUnitsInterval) {
			listener.enterErrorCapturingMultiUnitsInterval(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitErrorCapturingMultiUnitsInterval) {
			listener.exitErrorCapturingMultiUnitsInterval(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitErrorCapturingMultiUnitsInterval) {
			return visitor.visitErrorCapturingMultiUnitsInterval(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class MultiUnitsIntervalContext extends ParserRuleContext {
	public _identifier!: IdentifierContext;
	public _unit: IdentifierContext[] = [];
	public intervalValue(): IntervalValueContext[];
	public intervalValue(i: number): IntervalValueContext;
	public intervalValue(i?: number): IntervalValueContext | IntervalValueContext[] {
		if (i === undefined) {
			return this.getRuleContexts(IntervalValueContext);
		} else {
			return this.getRuleContext(i, IntervalValueContext);
		}
	}
	public identifier(): IdentifierContext[];
	public identifier(i: number): IdentifierContext;
	public identifier(i?: number): IdentifierContext | IdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(IdentifierContext);
		} else {
			return this.getRuleContext(i, IdentifierContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_multiUnitsInterval; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterMultiUnitsInterval) {
			listener.enterMultiUnitsInterval(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitMultiUnitsInterval) {
			listener.exitMultiUnitsInterval(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitMultiUnitsInterval) {
			return visitor.visitMultiUnitsInterval(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ErrorCapturingUnitToUnitIntervalContext extends ParserRuleContext {
	public _body!: UnitToUnitIntervalContext;
	public _error1!: MultiUnitsIntervalContext;
	public _error2!: UnitToUnitIntervalContext;
	public unitToUnitInterval(): UnitToUnitIntervalContext[];
	public unitToUnitInterval(i: number): UnitToUnitIntervalContext;
	public unitToUnitInterval(i?: number): UnitToUnitIntervalContext | UnitToUnitIntervalContext[] {
		if (i === undefined) {
			return this.getRuleContexts(UnitToUnitIntervalContext);
		} else {
			return this.getRuleContext(i, UnitToUnitIntervalContext);
		}
	}
	public multiUnitsInterval(): MultiUnitsIntervalContext | undefined {
		return this.tryGetRuleContext(0, MultiUnitsIntervalContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_errorCapturingUnitToUnitInterval; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterErrorCapturingUnitToUnitInterval) {
			listener.enterErrorCapturingUnitToUnitInterval(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitErrorCapturingUnitToUnitInterval) {
			listener.exitErrorCapturingUnitToUnitInterval(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitErrorCapturingUnitToUnitInterval) {
			return visitor.visitErrorCapturingUnitToUnitInterval(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class UnitToUnitIntervalContext extends ParserRuleContext {
	public _value!: IntervalValueContext;
	public _from!: IdentifierContext;
	public _to!: IdentifierContext;
	public TO(): TerminalNode { return this.getToken(SparkSqlParser.TO, 0); }
	public intervalValue(): IntervalValueContext {
		return this.getRuleContext(0, IntervalValueContext);
	}
	public identifier(): IdentifierContext[];
	public identifier(i: number): IdentifierContext;
	public identifier(i?: number): IdentifierContext | IdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(IdentifierContext);
		} else {
			return this.getRuleContext(i, IdentifierContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_unitToUnitInterval; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterUnitToUnitInterval) {
			listener.enterUnitToUnitInterval(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitUnitToUnitInterval) {
			listener.exitUnitToUnitInterval(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitUnitToUnitInterval) {
			return visitor.visitUnitToUnitInterval(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class IntervalValueContext extends ParserRuleContext {
	public INTEGER_VALUE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INTEGER_VALUE, 0); }
	public DECIMAL_VALUE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DECIMAL_VALUE, 0); }
	public PLUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PLUS, 0); }
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	public STRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRING, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_intervalValue; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterIntervalValue) {
			listener.enterIntervalValue(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitIntervalValue) {
			listener.exitIntervalValue(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitIntervalValue) {
			return visitor.visitIntervalValue(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ColPositionContext extends ParserRuleContext {
	public _position!: Token;
	public _afterCol!: ErrorCapturingIdentifierContext;
	public FIRST(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FIRST, 0); }
	public AFTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AFTER, 0); }
	public errorCapturingIdentifier(): ErrorCapturingIdentifierContext | undefined {
		return this.tryGetRuleContext(0, ErrorCapturingIdentifierContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_colPosition; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterColPosition) {
			listener.enterColPosition(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitColPosition) {
			listener.exitColPosition(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitColPosition) {
			return visitor.visitColPosition(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class DataTypeContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_dataType; }
	public copyFrom(ctx: DataTypeContext): void {
		super.copyFrom(ctx);
	}
}
export class ComplexDataTypeContext extends DataTypeContext {
	public _complex!: Token;
	public LT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LT, 0); }
	public dataType(): DataTypeContext[];
	public dataType(i: number): DataTypeContext;
	public dataType(i?: number): DataTypeContext | DataTypeContext[] {
		if (i === undefined) {
			return this.getRuleContexts(DataTypeContext);
		} else {
			return this.getRuleContext(i, DataTypeContext);
		}
	}
	public GT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.GT, 0); }
	public ARRAY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ARRAY, 0); }
	public MAP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MAP, 0); }
	public STRUCT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRUCT, 0); }
	public NEQ(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NEQ, 0); }
	public complexColTypeList(): ComplexColTypeListContext | undefined {
		return this.tryGetRuleContext(0, ComplexColTypeListContext);
	}
	constructor(ctx: DataTypeContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterComplexDataType) {
			listener.enterComplexDataType(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitComplexDataType) {
			listener.exitComplexDataType(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitComplexDataType) {
			return visitor.visitComplexDataType(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class PrimitiveDataTypeContext extends DataTypeContext {
	public identifier(): IdentifierContext {
		return this.getRuleContext(0, IdentifierContext);
	}
	public INTEGER_VALUE(): TerminalNode[];
	public INTEGER_VALUE(i: number): TerminalNode;
	public INTEGER_VALUE(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.INTEGER_VALUE);
		} else {
			return this.getToken(SparkSqlParser.INTEGER_VALUE, i);
		}
	}
	constructor(ctx: DataTypeContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterPrimitiveDataType) {
			listener.enterPrimitiveDataType(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitPrimitiveDataType) {
			listener.exitPrimitiveDataType(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitPrimitiveDataType) {
			return visitor.visitPrimitiveDataType(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class QualifiedColTypeWithPositionListContext extends ParserRuleContext {
	public qualifiedColTypeWithPosition(): QualifiedColTypeWithPositionContext[];
	public qualifiedColTypeWithPosition(i: number): QualifiedColTypeWithPositionContext;
	public qualifiedColTypeWithPosition(i?: number): QualifiedColTypeWithPositionContext | QualifiedColTypeWithPositionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(QualifiedColTypeWithPositionContext);
		} else {
			return this.getRuleContext(i, QualifiedColTypeWithPositionContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_qualifiedColTypeWithPositionList; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterQualifiedColTypeWithPositionList) {
			listener.enterQualifiedColTypeWithPositionList(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitQualifiedColTypeWithPositionList) {
			listener.exitQualifiedColTypeWithPositionList(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitQualifiedColTypeWithPositionList) {
			return visitor.visitQualifiedColTypeWithPositionList(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class QualifiedColTypeWithPositionContext extends ParserRuleContext {
	public _name!: MultipartIdentifierContext;
	public dataType(): DataTypeContext {
		return this.getRuleContext(0, DataTypeContext);
	}
	public multipartIdentifier(): MultipartIdentifierContext {
		return this.getRuleContext(0, MultipartIdentifierContext);
	}
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public NULL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NULL, 0); }
	public commentSpec(): CommentSpecContext | undefined {
		return this.tryGetRuleContext(0, CommentSpecContext);
	}
	public colPosition(): ColPositionContext | undefined {
		return this.tryGetRuleContext(0, ColPositionContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_qualifiedColTypeWithPosition; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterQualifiedColTypeWithPosition) {
			listener.enterQualifiedColTypeWithPosition(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitQualifiedColTypeWithPosition) {
			listener.exitQualifiedColTypeWithPosition(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitQualifiedColTypeWithPosition) {
			return visitor.visitQualifiedColTypeWithPosition(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ColTypeListContext extends ParserRuleContext {
	public colType(): ColTypeContext[];
	public colType(i: number): ColTypeContext;
	public colType(i?: number): ColTypeContext | ColTypeContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ColTypeContext);
		} else {
			return this.getRuleContext(i, ColTypeContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_colTypeList; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterColTypeList) {
			listener.enterColTypeList(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitColTypeList) {
			listener.exitColTypeList(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitColTypeList) {
			return visitor.visitColTypeList(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ColTypeContext extends ParserRuleContext {
	public _colName!: ErrorCapturingIdentifierContext;
	public dataType(): DataTypeContext {
		return this.getRuleContext(0, DataTypeContext);
	}
	public errorCapturingIdentifier(): ErrorCapturingIdentifierContext {
		return this.getRuleContext(0, ErrorCapturingIdentifierContext);
	}
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public NULL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NULL, 0); }
	public commentSpec(): CommentSpecContext | undefined {
		return this.tryGetRuleContext(0, CommentSpecContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_colType; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterColType) {
			listener.enterColType(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitColType) {
			listener.exitColType(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitColType) {
			return visitor.visitColType(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ComplexColTypeListContext extends ParserRuleContext {
	public complexColType(): ComplexColTypeContext[];
	public complexColType(i: number): ComplexColTypeContext;
	public complexColType(i?: number): ComplexColTypeContext | ComplexColTypeContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ComplexColTypeContext);
		} else {
			return this.getRuleContext(i, ComplexColTypeContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_complexColTypeList; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterComplexColTypeList) {
			listener.enterComplexColTypeList(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitComplexColTypeList) {
			listener.exitComplexColTypeList(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitComplexColTypeList) {
			return visitor.visitComplexColTypeList(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ComplexColTypeContext extends ParserRuleContext {
	public identifier(): IdentifierContext {
		return this.getRuleContext(0, IdentifierContext);
	}
	public dataType(): DataTypeContext {
		return this.getRuleContext(0, DataTypeContext);
	}
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public NULL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NULL, 0); }
	public commentSpec(): CommentSpecContext | undefined {
		return this.tryGetRuleContext(0, CommentSpecContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_complexColType; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterComplexColType) {
			listener.enterComplexColType(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitComplexColType) {
			listener.exitComplexColType(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitComplexColType) {
			return visitor.visitComplexColType(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class WhenClauseContext extends ParserRuleContext {
	public _condition!: ExpressionContext;
	public _result!: ExpressionContext;
	public WHEN(): TerminalNode { return this.getToken(SparkSqlParser.WHEN, 0); }
	public THEN(): TerminalNode { return this.getToken(SparkSqlParser.THEN, 0); }
	public expression(): ExpressionContext[];
	public expression(i: number): ExpressionContext;
	public expression(i?: number): ExpressionContext | ExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ExpressionContext);
		} else {
			return this.getRuleContext(i, ExpressionContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_whenClause; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterWhenClause) {
			listener.enterWhenClause(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitWhenClause) {
			listener.exitWhenClause(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitWhenClause) {
			return visitor.visitWhenClause(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class WindowClauseContext extends ParserRuleContext {
	public WINDOW(): TerminalNode { return this.getToken(SparkSqlParser.WINDOW, 0); }
	public namedWindow(): NamedWindowContext[];
	public namedWindow(i: number): NamedWindowContext;
	public namedWindow(i?: number): NamedWindowContext | NamedWindowContext[] {
		if (i === undefined) {
			return this.getRuleContexts(NamedWindowContext);
		} else {
			return this.getRuleContext(i, NamedWindowContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_windowClause; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterWindowClause) {
			listener.enterWindowClause(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitWindowClause) {
			listener.exitWindowClause(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitWindowClause) {
			return visitor.visitWindowClause(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class NamedWindowContext extends ParserRuleContext {
	public _name!: ErrorCapturingIdentifierContext;
	public AS(): TerminalNode { return this.getToken(SparkSqlParser.AS, 0); }
	public windowSpec(): WindowSpecContext {
		return this.getRuleContext(0, WindowSpecContext);
	}
	public errorCapturingIdentifier(): ErrorCapturingIdentifierContext {
		return this.getRuleContext(0, ErrorCapturingIdentifierContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_namedWindow; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterNamedWindow) {
			listener.enterNamedWindow(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitNamedWindow) {
			listener.exitNamedWindow(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitNamedWindow) {
			return visitor.visitNamedWindow(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class WindowSpecContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_windowSpec; }
	public copyFrom(ctx: WindowSpecContext): void {
		super.copyFrom(ctx);
	}
}
export class WindowRefContext extends WindowSpecContext {
	public _name!: ErrorCapturingIdentifierContext;
	public errorCapturingIdentifier(): ErrorCapturingIdentifierContext {
		return this.getRuleContext(0, ErrorCapturingIdentifierContext);
	}
	constructor(ctx: WindowSpecContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterWindowRef) {
			listener.enterWindowRef(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitWindowRef) {
			listener.exitWindowRef(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitWindowRef) {
			return visitor.visitWindowRef(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class WindowDefContext extends WindowSpecContext {
	public _expression!: ExpressionContext;
	public _partition: ExpressionContext[] = [];
	public CLUSTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CLUSTER, 0); }
	public BY(): TerminalNode[];
	public BY(i: number): TerminalNode;
	public BY(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.BY);
		} else {
			return this.getToken(SparkSqlParser.BY, i);
		}
	}
	public expression(): ExpressionContext[];
	public expression(i: number): ExpressionContext;
	public expression(i?: number): ExpressionContext | ExpressionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ExpressionContext);
		} else {
			return this.getRuleContext(i, ExpressionContext);
		}
	}
	public windowFrame(): WindowFrameContext | undefined {
		return this.tryGetRuleContext(0, WindowFrameContext);
	}
	public sortItem(): SortItemContext[];
	public sortItem(i: number): SortItemContext;
	public sortItem(i?: number): SortItemContext | SortItemContext[] {
		if (i === undefined) {
			return this.getRuleContexts(SortItemContext);
		} else {
			return this.getRuleContext(i, SortItemContext);
		}
	}
	public PARTITION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PARTITION, 0); }
	public DISTRIBUTE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DISTRIBUTE, 0); }
	public ORDER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ORDER, 0); }
	public SORT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SORT, 0); }
	constructor(ctx: WindowSpecContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterWindowDef) {
			listener.enterWindowDef(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitWindowDef) {
			listener.exitWindowDef(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitWindowDef) {
			return visitor.visitWindowDef(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class WindowFrameContext extends ParserRuleContext {
	public _frameType!: Token;
	public _start!: FrameBoundContext;
	public _end!: FrameBoundContext;
	public RANGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RANGE, 0); }
	public frameBound(): FrameBoundContext[];
	public frameBound(i: number): FrameBoundContext;
	public frameBound(i?: number): FrameBoundContext | FrameBoundContext[] {
		if (i === undefined) {
			return this.getRuleContexts(FrameBoundContext);
		} else {
			return this.getRuleContext(i, FrameBoundContext);
		}
	}
	public ROWS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROWS, 0); }
	public BETWEEN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.BETWEEN, 0); }
	public AND(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AND, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_windowFrame; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterWindowFrame) {
			listener.enterWindowFrame(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitWindowFrame) {
			listener.exitWindowFrame(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitWindowFrame) {
			return visitor.visitWindowFrame(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class FrameBoundContext extends ParserRuleContext {
	public _boundType!: Token;
	public UNBOUNDED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNBOUNDED, 0); }
	public PRECEDING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PRECEDING, 0); }
	public FOLLOWING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FOLLOWING, 0); }
	public ROW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROW, 0); }
	public CURRENT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CURRENT, 0); }
	public expression(): ExpressionContext | undefined {
		return this.tryGetRuleContext(0, ExpressionContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_frameBound; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterFrameBound) {
			listener.enterFrameBound(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitFrameBound) {
			listener.exitFrameBound(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitFrameBound) {
			return visitor.visitFrameBound(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class QualifiedNameListContext extends ParserRuleContext {
	public qualifiedName(): QualifiedNameContext[];
	public qualifiedName(i: number): QualifiedNameContext;
	public qualifiedName(i?: number): QualifiedNameContext | QualifiedNameContext[] {
		if (i === undefined) {
			return this.getRuleContexts(QualifiedNameContext);
		} else {
			return this.getRuleContext(i, QualifiedNameContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_qualifiedNameList; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterQualifiedNameList) {
			listener.enterQualifiedNameList(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitQualifiedNameList) {
			listener.exitQualifiedNameList(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitQualifiedNameList) {
			return visitor.visitQualifiedNameList(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class FunctionNameContext extends ParserRuleContext {
	public qualifiedName(): QualifiedNameContext | undefined {
		return this.tryGetRuleContext(0, QualifiedNameContext);
	}
	public FILTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FILTER, 0); }
	public LEFT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LEFT, 0); }
	public RIGHT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RIGHT, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_functionName; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterFunctionName) {
			listener.enterFunctionName(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitFunctionName) {
			listener.exitFunctionName(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitFunctionName) {
			return visitor.visitFunctionName(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class QualifiedNameContext extends ParserRuleContext {
	public identifier(): IdentifierContext[];
	public identifier(i: number): IdentifierContext;
	public identifier(i?: number): IdentifierContext | IdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(IdentifierContext);
		} else {
			return this.getRuleContext(i, IdentifierContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_qualifiedName; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterQualifiedName) {
			listener.enterQualifiedName(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitQualifiedName) {
			listener.exitQualifiedName(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitQualifiedName) {
			return visitor.visitQualifiedName(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ErrorCapturingIdentifierContext extends ParserRuleContext {
	public identifier(): IdentifierContext {
		return this.getRuleContext(0, IdentifierContext);
	}
	public errorCapturingIdentifierExtra(): ErrorCapturingIdentifierExtraContext {
		return this.getRuleContext(0, ErrorCapturingIdentifierExtraContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_errorCapturingIdentifier; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterErrorCapturingIdentifier) {
			listener.enterErrorCapturingIdentifier(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitErrorCapturingIdentifier) {
			listener.exitErrorCapturingIdentifier(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitErrorCapturingIdentifier) {
			return visitor.visitErrorCapturingIdentifier(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ErrorCapturingIdentifierExtraContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_errorCapturingIdentifierExtra; }
	public copyFrom(ctx: ErrorCapturingIdentifierExtraContext): void {
		super.copyFrom(ctx);
	}
}
export class ErrorIdentContext extends ErrorCapturingIdentifierExtraContext {
	public MINUS(): TerminalNode[];
	public MINUS(i: number): TerminalNode;
	public MINUS(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SparkSqlParser.MINUS);
		} else {
			return this.getToken(SparkSqlParser.MINUS, i);
		}
	}
	public identifier(): IdentifierContext[];
	public identifier(i: number): IdentifierContext;
	public identifier(i?: number): IdentifierContext | IdentifierContext[] {
		if (i === undefined) {
			return this.getRuleContexts(IdentifierContext);
		} else {
			return this.getRuleContext(i, IdentifierContext);
		}
	}
	constructor(ctx: ErrorCapturingIdentifierExtraContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterErrorIdent) {
			listener.enterErrorIdent(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitErrorIdent) {
			listener.exitErrorIdent(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitErrorIdent) {
			return visitor.visitErrorIdent(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class RealIdentContext extends ErrorCapturingIdentifierExtraContext {
	constructor(ctx: ErrorCapturingIdentifierExtraContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterRealIdent) {
			listener.enterRealIdent(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitRealIdent) {
			listener.exitRealIdent(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitRealIdent) {
			return visitor.visitRealIdent(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class IdentifierContext extends ParserRuleContext {
	public strictIdentifier(): StrictIdentifierContext | undefined {
		return this.tryGetRuleContext(0, StrictIdentifierContext);
	}
	public strictNonReserved(): StrictNonReservedContext | undefined {
		return this.tryGetRuleContext(0, StrictNonReservedContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_identifier; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterIdentifier) {
			listener.enterIdentifier(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitIdentifier) {
			listener.exitIdentifier(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitIdentifier) {
			return visitor.visitIdentifier(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class StrictIdentifierContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_strictIdentifier; }
	public copyFrom(ctx: StrictIdentifierContext): void {
		super.copyFrom(ctx);
	}
}
export class UnquotedIdentifierContext extends StrictIdentifierContext {
	public IDENTIFIER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IDENTIFIER, 0); }
	public ansiNonReserved(): AnsiNonReservedContext | undefined {
		return this.tryGetRuleContext(0, AnsiNonReservedContext);
	}
	public nonReserved(): NonReservedContext | undefined {
		return this.tryGetRuleContext(0, NonReservedContext);
	}
	constructor(ctx: StrictIdentifierContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterUnquotedIdentifier) {
			listener.enterUnquotedIdentifier(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitUnquotedIdentifier) {
			listener.exitUnquotedIdentifier(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitUnquotedIdentifier) {
			return visitor.visitUnquotedIdentifier(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class QuotedIdentifierAlternativeContext extends StrictIdentifierContext {
	public quotedIdentifier(): QuotedIdentifierContext {
		return this.getRuleContext(0, QuotedIdentifierContext);
	}
	constructor(ctx: StrictIdentifierContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterQuotedIdentifierAlternative) {
			listener.enterQuotedIdentifierAlternative(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitQuotedIdentifierAlternative) {
			listener.exitQuotedIdentifierAlternative(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitQuotedIdentifierAlternative) {
			return visitor.visitQuotedIdentifierAlternative(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class QuotedIdentifierContext extends ParserRuleContext {
	public BACKQUOTED_IDENTIFIER(): TerminalNode { return this.getToken(SparkSqlParser.BACKQUOTED_IDENTIFIER, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_quotedIdentifier; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterQuotedIdentifier) {
			listener.enterQuotedIdentifier(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitQuotedIdentifier) {
			listener.exitQuotedIdentifier(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitQuotedIdentifier) {
			return visitor.visitQuotedIdentifier(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class NumberContext extends ParserRuleContext {
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_number; }
	public copyFrom(ctx: NumberContext): void {
		super.copyFrom(ctx);
	}
}
export class ExponentLiteralContext extends NumberContext {
	public EXPONENT_VALUE(): TerminalNode { return this.getToken(SparkSqlParser.EXPONENT_VALUE, 0); }
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	constructor(ctx: NumberContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterExponentLiteral) {
			listener.enterExponentLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitExponentLiteral) {
			listener.exitExponentLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitExponentLiteral) {
			return visitor.visitExponentLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DecimalLiteralContext extends NumberContext {
	public DECIMAL_VALUE(): TerminalNode { return this.getToken(SparkSqlParser.DECIMAL_VALUE, 0); }
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	constructor(ctx: NumberContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDecimalLiteral) {
			listener.enterDecimalLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDecimalLiteral) {
			listener.exitDecimalLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDecimalLiteral) {
			return visitor.visitDecimalLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class LegacyDecimalLiteralContext extends NumberContext {
	public EXPONENT_VALUE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXPONENT_VALUE, 0); }
	public DECIMAL_VALUE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DECIMAL_VALUE, 0); }
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	constructor(ctx: NumberContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterLegacyDecimalLiteral) {
			listener.enterLegacyDecimalLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitLegacyDecimalLiteral) {
			listener.exitLegacyDecimalLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitLegacyDecimalLiteral) {
			return visitor.visitLegacyDecimalLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class IntegerLiteralContext extends NumberContext {
	public INTEGER_VALUE(): TerminalNode { return this.getToken(SparkSqlParser.INTEGER_VALUE, 0); }
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	constructor(ctx: NumberContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterIntegerLiteral) {
			listener.enterIntegerLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitIntegerLiteral) {
			listener.exitIntegerLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitIntegerLiteral) {
			return visitor.visitIntegerLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class BigIntLiteralContext extends NumberContext {
	public BIGINT_LITERAL(): TerminalNode { return this.getToken(SparkSqlParser.BIGINT_LITERAL, 0); }
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	constructor(ctx: NumberContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterBigIntLiteral) {
			listener.enterBigIntLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitBigIntLiteral) {
			listener.exitBigIntLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitBigIntLiteral) {
			return visitor.visitBigIntLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class SmallIntLiteralContext extends NumberContext {
	public SMALLINT_LITERAL(): TerminalNode { return this.getToken(SparkSqlParser.SMALLINT_LITERAL, 0); }
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	constructor(ctx: NumberContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterSmallIntLiteral) {
			listener.enterSmallIntLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitSmallIntLiteral) {
			listener.exitSmallIntLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitSmallIntLiteral) {
			return visitor.visitSmallIntLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class TinyIntLiteralContext extends NumberContext {
	public TINYINT_LITERAL(): TerminalNode { return this.getToken(SparkSqlParser.TINYINT_LITERAL, 0); }
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	constructor(ctx: NumberContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterTinyIntLiteral) {
			listener.enterTinyIntLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitTinyIntLiteral) {
			listener.exitTinyIntLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitTinyIntLiteral) {
			return visitor.visitTinyIntLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class DoubleLiteralContext extends NumberContext {
	public DOUBLE_LITERAL(): TerminalNode { return this.getToken(SparkSqlParser.DOUBLE_LITERAL, 0); }
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	constructor(ctx: NumberContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterDoubleLiteral) {
			listener.enterDoubleLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitDoubleLiteral) {
			listener.exitDoubleLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitDoubleLiteral) {
			return visitor.visitDoubleLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class FloatLiteralContext extends NumberContext {
	public FLOAT_LITERAL(): TerminalNode { return this.getToken(SparkSqlParser.FLOAT_LITERAL, 0); }
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	constructor(ctx: NumberContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterFloatLiteral) {
			listener.enterFloatLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitFloatLiteral) {
			listener.exitFloatLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitFloatLiteral) {
			return visitor.visitFloatLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
export class BigDecimalLiteralContext extends NumberContext {
	public BIGDECIMAL_LITERAL(): TerminalNode { return this.getToken(SparkSqlParser.BIGDECIMAL_LITERAL, 0); }
	public MINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MINUS, 0); }
	constructor(ctx: NumberContext) {
		super(ctx.parent, ctx.invokingState);
		this.copyFrom(ctx);
	}
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterBigDecimalLiteral) {
			listener.enterBigDecimalLiteral(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitBigDecimalLiteral) {
			listener.exitBigDecimalLiteral(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitBigDecimalLiteral) {
			return visitor.visitBigDecimalLiteral(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class AlterColumnActionContext extends ParserRuleContext {
	public _setOrDrop!: Token;
	public TYPE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TYPE, 0); }
	public dataType(): DataTypeContext | undefined {
		return this.tryGetRuleContext(0, DataTypeContext);
	}
	public commentSpec(): CommentSpecContext | undefined {
		return this.tryGetRuleContext(0, CommentSpecContext);
	}
	public colPosition(): ColPositionContext | undefined {
		return this.tryGetRuleContext(0, ColPositionContext);
	}
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public NULL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NULL, 0); }
	public SET(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SET, 0); }
	public DROP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DROP, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_alterColumnAction; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterAlterColumnAction) {
			listener.enterAlterColumnAction(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitAlterColumnAction) {
			listener.exitAlterColumnAction(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitAlterColumnAction) {
			return visitor.visitAlterColumnAction(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class AnsiNonReservedContext extends ParserRuleContext {
	public ADD(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ADD, 0); }
	public AFTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AFTER, 0); }
	public ALTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ALTER, 0); }
	public ANALYZE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ANALYZE, 0); }
	public ANTI(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ANTI, 0); }
	public ARCHIVE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ARCHIVE, 0); }
	public ARRAY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ARRAY, 0); }
	public ASC(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ASC, 0); }
	public AT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AT, 0); }
	public BETWEEN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.BETWEEN, 0); }
	public BUCKET(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.BUCKET, 0); }
	public BUCKETS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.BUCKETS, 0); }
	public BY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.BY, 0); }
	public CACHE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CACHE, 0); }
	public CASCADE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CASCADE, 0); }
	public CHANGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CHANGE, 0); }
	public CLEAR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CLEAR, 0); }
	public CLUSTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CLUSTER, 0); }
	public CLUSTERED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CLUSTERED, 0); }
	public CODEGEN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CODEGEN, 0); }
	public COLLECTION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLLECTION, 0); }
	public COLUMNS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLUMNS, 0); }
	public COMMENT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COMMENT, 0); }
	public COMMIT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COMMIT, 0); }
	public COMPACT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COMPACT, 0); }
	public COMPACTIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COMPACTIONS, 0); }
	public COMPUTE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COMPUTE, 0); }
	public CONCATENATE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CONCATENATE, 0); }
	public COST(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COST, 0); }
	public CUBE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CUBE, 0); }
	public CURRENT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CURRENT, 0); }
	public DATA(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DATA, 0); }
	public DATABASE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DATABASE, 0); }
	public DATABASES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DATABASES, 0); }
	public DBPROPERTIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DBPROPERTIES, 0); }
	public DEFINED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DEFINED, 0); }
	public DELETE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DELETE, 0); }
	public DELIMITED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DELIMITED, 0); }
	public DESC(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DESC, 0); }
	public DESCRIBE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DESCRIBE, 0); }
	public DFS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DFS, 0); }
	public DIRECTORIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DIRECTORIES, 0); }
	public DIRECTORY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DIRECTORY, 0); }
	public DISTRIBUTE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DISTRIBUTE, 0); }
	public DIV(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DIV, 0); }
	public DROP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DROP, 0); }
	public ESCAPED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ESCAPED, 0); }
	public EXCHANGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXCHANGE, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	public EXPLAIN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXPLAIN, 0); }
	public EXPORT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXPORT, 0); }
	public EXTENDED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXTENDED, 0); }
	public EXTERNAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXTERNAL, 0); }
	public EXTRACT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXTRACT, 0); }
	public FIELDS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FIELDS, 0); }
	public FILEFORMAT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FILEFORMAT, 0); }
	public FIRST(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FIRST, 0); }
	public FOLLOWING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FOLLOWING, 0); }
	public FORMAT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FORMAT, 0); }
	public FORMATTED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FORMATTED, 0); }
	public FUNCTION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FUNCTION, 0); }
	public FUNCTIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FUNCTIONS, 0); }
	public GLOBAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.GLOBAL, 0); }
	public GROUPING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.GROUPING, 0); }
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public IGNORE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IGNORE, 0); }
	public IMPORT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IMPORT, 0); }
	public INDEX(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INDEX, 0); }
	public INDEXES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INDEXES, 0); }
	public INPATH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INPATH, 0); }
	public INPUTFORMAT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INPUTFORMAT, 0); }
	public INSERT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INSERT, 0); }
	public INTERVAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INTERVAL, 0); }
	public ITEMS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ITEMS, 0); }
	public KEYS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.KEYS, 0); }
	public LAST(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LAST, 0); }
	public LATERAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LATERAL, 0); }
	public LAZY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LAZY, 0); }
	public LIKE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LIKE, 0); }
	public LIMIT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LIMIT, 0); }
	public LINES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LINES, 0); }
	public LIST(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LIST, 0); }
	public LOAD(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOAD, 0); }
	public LOCAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCAL, 0); }
	public LOCATION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCATION, 0); }
	public LOCK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCK, 0); }
	public LOCKS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCKS, 0); }
	public LOGICAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOGICAL, 0); }
	public MACRO(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MACRO, 0); }
	public MAP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MAP, 0); }
	public MATCHED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MATCHED, 0); }
	public MERGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MERGE, 0); }
	public MSCK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MSCK, 0); }
	public NAMESPACE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NAMESPACE, 0); }
	public NAMESPACES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NAMESPACES, 0); }
	public NO(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NO, 0); }
	public NULLS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NULLS, 0); }
	public OF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OF, 0); }
	public OPTION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OPTION, 0); }
	public OPTIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OPTIONS, 0); }
	public OUT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OUT, 0); }
	public OUTPUTFORMAT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OUTPUTFORMAT, 0); }
	public OVER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OVER, 0); }
	public OVERLAY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OVERLAY, 0); }
	public OVERWRITE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OVERWRITE, 0); }
	public PARTITION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PARTITION, 0); }
	public PARTITIONED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PARTITIONED, 0); }
	public PARTITIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PARTITIONS, 0); }
	public PERCENTLIT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PERCENTLIT, 0); }
	public PIVOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PIVOT, 0); }
	public PLACING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PLACING, 0); }
	public POSITION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.POSITION, 0); }
	public PRECEDING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PRECEDING, 0); }
	public PRINCIPALS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PRINCIPALS, 0); }
	public PROPERTIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PROPERTIES, 0); }
	public PURGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PURGE, 0); }
	public QUERY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.QUERY, 0); }
	public RANGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RANGE, 0); }
	public RECORDREADER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RECORDREADER, 0); }
	public RECORDWRITER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RECORDWRITER, 0); }
	public RECOVER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RECOVER, 0); }
	public REDUCE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REDUCE, 0); }
	public REFRESH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REFRESH, 0); }
	public RENAME(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RENAME, 0); }
	public REPAIR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REPAIR, 0); }
	public REPLACE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REPLACE, 0); }
	public RESET(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RESET, 0); }
	public RESTRICT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RESTRICT, 0); }
	public REVOKE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REVOKE, 0); }
	public RLIKE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RLIKE, 0); }
	public ROLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROLE, 0); }
	public ROLES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROLES, 0); }
	public ROLLBACK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROLLBACK, 0); }
	public ROLLUP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROLLUP, 0); }
	public ROW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROW, 0); }
	public ROWS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROWS, 0); }
	public SCHEMA(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SCHEMA, 0); }
	public SEMI(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SEMI, 0); }
	public SEPARATED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SEPARATED, 0); }
	public SERDE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SERDE, 0); }
	public SERDEPROPERTIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SERDEPROPERTIES, 0); }
	public SET(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SET, 0); }
	public SETMINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SETMINUS, 0); }
	public SETS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SETS, 0); }
	public SHOW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SHOW, 0); }
	public SKEWED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SKEWED, 0); }
	public SORT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SORT, 0); }
	public SORTED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SORTED, 0); }
	public START(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.START, 0); }
	public STATISTICS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STATISTICS, 0); }
	public STORED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STORED, 0); }
	public STRATIFY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRATIFY, 0); }
	public STRUCT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRUCT, 0); }
	public SUBSTR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SUBSTR, 0); }
	public SUBSTRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SUBSTRING, 0); }
	public TABLES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TABLES, 0); }
	public TABLESAMPLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TABLESAMPLE, 0); }
	public TBLPROPERTIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TBLPROPERTIES, 0); }
	public TEMPORARY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TEMPORARY, 0); }
	public TERMINATED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TERMINATED, 0); }
	public TOUCH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TOUCH, 0); }
	public TRANSACTION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRANSACTION, 0); }
	public TRANSACTIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRANSACTIONS, 0); }
	public TRANSFORM(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRANSFORM, 0); }
	public TRIM(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRIM, 0); }
	public TRUE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRUE, 0); }
	public TRUNCATE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRUNCATE, 0); }
	public TYPE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TYPE, 0); }
	public UNARCHIVE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNARCHIVE, 0); }
	public UNBOUNDED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNBOUNDED, 0); }
	public UNCACHE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNCACHE, 0); }
	public UNLOCK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNLOCK, 0); }
	public UNSET(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNSET, 0); }
	public UPDATE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UPDATE, 0); }
	public USE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.USE, 0); }
	public VALUES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.VALUES, 0); }
	public VIEW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.VIEW, 0); }
	public VIEWS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.VIEWS, 0); }
	public WINDOW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.WINDOW, 0); }
	public ZONE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ZONE, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_ansiNonReserved; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterAnsiNonReserved) {
			listener.enterAnsiNonReserved(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitAnsiNonReserved) {
			listener.exitAnsiNonReserved(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitAnsiNonReserved) {
			return visitor.visitAnsiNonReserved(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class StrictNonReservedContext extends ParserRuleContext {
	public ANTI(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ANTI, 0); }
	public CROSS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CROSS, 0); }
	public EXCEPT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXCEPT, 0); }
	public FULL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FULL, 0); }
	public INNER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INNER, 0); }
	public INTERSECT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INTERSECT, 0); }
	public JOIN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.JOIN, 0); }
	public LEFT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LEFT, 0); }
	public NATURAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NATURAL, 0); }
	public ON(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ON, 0); }
	public RIGHT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RIGHT, 0); }
	public SEMI(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SEMI, 0); }
	public SETMINUS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SETMINUS, 0); }
	public UNION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNION, 0); }
	public USING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.USING, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_strictNonReserved; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterStrictNonReserved) {
			listener.enterStrictNonReserved(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitStrictNonReserved) {
			listener.exitStrictNonReserved(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitStrictNonReserved) {
			return visitor.visitStrictNonReserved(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class NonReservedContext extends ParserRuleContext {
	public ADD(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ADD, 0); }
	public AFTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AFTER, 0); }
	public ALL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ALL, 0); }
	public ALTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ALTER, 0); }
	public ANALYZE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ANALYZE, 0); }
	public AND(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AND, 0); }
	public ANY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ANY, 0); }
	public ARCHIVE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ARCHIVE, 0); }
	public ARRAY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ARRAY, 0); }
	public AS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AS, 0); }
	public ASC(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ASC, 0); }
	public AT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AT, 0); }
	public AUTHORIZATION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.AUTHORIZATION, 0); }
	public BETWEEN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.BETWEEN, 0); }
	public BOTH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.BOTH, 0); }
	public BUCKET(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.BUCKET, 0); }
	public BUCKETS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.BUCKETS, 0); }
	public BY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.BY, 0); }
	public CACHE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CACHE, 0); }
	public CASCADE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CASCADE, 0); }
	public CASE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CASE, 0); }
	public CAST(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CAST, 0); }
	public CHANGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CHANGE, 0); }
	public CHECK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CHECK, 0); }
	public CLEAR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CLEAR, 0); }
	public CLUSTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CLUSTER, 0); }
	public CLUSTERED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CLUSTERED, 0); }
	public CODEGEN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CODEGEN, 0); }
	public COLLATE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLLATE, 0); }
	public COLLECTION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLLECTION, 0); }
	public COLUMN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLUMN, 0); }
	public COLUMNS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COLUMNS, 0); }
	public COMMENT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COMMENT, 0); }
	public COMMIT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COMMIT, 0); }
	public COMPACT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COMPACT, 0); }
	public COMPACTIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COMPACTIONS, 0); }
	public COMPUTE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COMPUTE, 0); }
	public CONCATENATE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CONCATENATE, 0); }
	public CONSTRAINT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CONSTRAINT, 0); }
	public COST(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.COST, 0); }
	public CREATE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CREATE, 0); }
	public CUBE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CUBE, 0); }
	public CURRENT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CURRENT, 0); }
	public CURRENT_DATE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CURRENT_DATE, 0); }
	public CURRENT_TIME(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CURRENT_TIME, 0); }
	public CURRENT_TIMESTAMP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CURRENT_TIMESTAMP, 0); }
	public CURRENT_USER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.CURRENT_USER, 0); }
	public DATA(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DATA, 0); }
	public DATABASE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DATABASE, 0); }
	public DATABASES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DATABASES, 0); }
	public DBPROPERTIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DBPROPERTIES, 0); }
	public DEFINED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DEFINED, 0); }
	public DELETE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DELETE, 0); }
	public DELIMITED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DELIMITED, 0); }
	public DESC(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DESC, 0); }
	public DESCRIBE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DESCRIBE, 0); }
	public DFS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DFS, 0); }
	public DIRECTORIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DIRECTORIES, 0); }
	public DIRECTORY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DIRECTORY, 0); }
	public DISTINCT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DISTINCT, 0); }
	public DISTRIBUTE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DISTRIBUTE, 0); }
	public DIV(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DIV, 0); }
	public DROP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.DROP, 0); }
	public ELSE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ELSE, 0); }
	public END(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.END, 0); }
	public ESCAPE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ESCAPE, 0); }
	public ESCAPED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ESCAPED, 0); }
	public EXCHANGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXCHANGE, 0); }
	public EXISTS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXISTS, 0); }
	public EXPLAIN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXPLAIN, 0); }
	public EXPORT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXPORT, 0); }
	public EXTENDED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXTENDED, 0); }
	public EXTERNAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXTERNAL, 0); }
	public EXTRACT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.EXTRACT, 0); }
	public FALSE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FALSE, 0); }
	public FETCH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FETCH, 0); }
	public FILTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FILTER, 0); }
	public FIELDS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FIELDS, 0); }
	public FILEFORMAT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FILEFORMAT, 0); }
	public FIRST(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FIRST, 0); }
	public FOLLOWING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FOLLOWING, 0); }
	public FOR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FOR, 0); }
	public FOREIGN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FOREIGN, 0); }
	public FORMAT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FORMAT, 0); }
	public FORMATTED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FORMATTED, 0); }
	public FROM(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FROM, 0); }
	public FUNCTION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FUNCTION, 0); }
	public FUNCTIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.FUNCTIONS, 0); }
	public GLOBAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.GLOBAL, 0); }
	public GRANT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.GRANT, 0); }
	public GROUP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.GROUP, 0); }
	public GROUPING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.GROUPING, 0); }
	public HAVING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.HAVING, 0); }
	public IF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IF, 0); }
	public IGNORE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IGNORE, 0); }
	public IMPORT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IMPORT, 0); }
	public IN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IN, 0); }
	public INDEX(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INDEX, 0); }
	public INDEXES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INDEXES, 0); }
	public INPATH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INPATH, 0); }
	public INPUTFORMAT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INPUTFORMAT, 0); }
	public INSERT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INSERT, 0); }
	public INTERVAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INTERVAL, 0); }
	public INTO(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.INTO, 0); }
	public IS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.IS, 0); }
	public ITEMS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ITEMS, 0); }
	public KEYS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.KEYS, 0); }
	public LAST(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LAST, 0); }
	public LATERAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LATERAL, 0); }
	public LAZY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LAZY, 0); }
	public LEADING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LEADING, 0); }
	public LIKE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LIKE, 0); }
	public LIMIT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LIMIT, 0); }
	public LINES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LINES, 0); }
	public LIST(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LIST, 0); }
	public LOAD(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOAD, 0); }
	public LOCAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCAL, 0); }
	public LOCATION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCATION, 0); }
	public LOCK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCK, 0); }
	public LOCKS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOCKS, 0); }
	public LOGICAL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.LOGICAL, 0); }
	public MACRO(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MACRO, 0); }
	public MAP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MAP, 0); }
	public MATCHED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MATCHED, 0); }
	public MERGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MERGE, 0); }
	public MSCK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.MSCK, 0); }
	public NAMESPACE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NAMESPACE, 0); }
	public NAMESPACES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NAMESPACES, 0); }
	public NO(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NO, 0); }
	public NOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NOT, 0); }
	public NULL(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NULL, 0); }
	public NULLS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.NULLS, 0); }
	public OF(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OF, 0); }
	public ONLY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ONLY, 0); }
	public OPTION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OPTION, 0); }
	public OPTIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OPTIONS, 0); }
	public OR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OR, 0); }
	public ORDER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ORDER, 0); }
	public OUT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OUT, 0); }
	public OUTER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OUTER, 0); }
	public OUTPUTFORMAT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OUTPUTFORMAT, 0); }
	public OVER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OVER, 0); }
	public OVERLAPS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OVERLAPS, 0); }
	public OVERLAY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OVERLAY, 0); }
	public OVERWRITE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.OVERWRITE, 0); }
	public PARTITION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PARTITION, 0); }
	public PARTITIONED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PARTITIONED, 0); }
	public PARTITIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PARTITIONS, 0); }
	public PERCENTLIT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PERCENTLIT, 0); }
	public PIVOT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PIVOT, 0); }
	public PLACING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PLACING, 0); }
	public POSITION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.POSITION, 0); }
	public PRECEDING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PRECEDING, 0); }
	public PRIMARY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PRIMARY, 0); }
	public PRINCIPALS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PRINCIPALS, 0); }
	public PROPERTIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PROPERTIES, 0); }
	public PURGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.PURGE, 0); }
	public QUERY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.QUERY, 0); }
	public RANGE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RANGE, 0); }
	public RECORDREADER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RECORDREADER, 0); }
	public RECORDWRITER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RECORDWRITER, 0); }
	public RECOVER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RECOVER, 0); }
	public REDUCE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REDUCE, 0); }
	public REFERENCES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REFERENCES, 0); }
	public REFRESH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REFRESH, 0); }
	public RENAME(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RENAME, 0); }
	public REPAIR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REPAIR, 0); }
	public REPLACE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REPLACE, 0); }
	public RESET(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RESET, 0); }
	public RESTRICT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RESTRICT, 0); }
	public REVOKE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.REVOKE, 0); }
	public RLIKE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.RLIKE, 0); }
	public ROLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROLE, 0); }
	public ROLES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROLES, 0); }
	public ROLLBACK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROLLBACK, 0); }
	public ROLLUP(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROLLUP, 0); }
	public ROW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROW, 0); }
	public ROWS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ROWS, 0); }
	public SCHEMA(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SCHEMA, 0); }
	public SELECT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SELECT, 0); }
	public SEPARATED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SEPARATED, 0); }
	public SERDE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SERDE, 0); }
	public SERDEPROPERTIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SERDEPROPERTIES, 0); }
	public SESSION_USER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SESSION_USER, 0); }
	public SET(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SET, 0); }
	public SETS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SETS, 0); }
	public SHOW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SHOW, 0); }
	public SKEWED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SKEWED, 0); }
	public SOME(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SOME, 0); }
	public SORT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SORT, 0); }
	public SORTED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SORTED, 0); }
	public START(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.START, 0); }
	public STATISTICS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STATISTICS, 0); }
	public STORED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STORED, 0); }
	public STRATIFY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRATIFY, 0); }
	public STRUCT(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.STRUCT, 0); }
	public SUBSTR(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SUBSTR, 0); }
	public SUBSTRING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.SUBSTRING, 0); }
	public TABLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TABLE, 0); }
	public TABLES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TABLES, 0); }
	public TABLESAMPLE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TABLESAMPLE, 0); }
	public TBLPROPERTIES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TBLPROPERTIES, 0); }
	public TEMPORARY(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TEMPORARY, 0); }
	public TERMINATED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TERMINATED, 0); }
	public THEN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.THEN, 0); }
	public TIME(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TIME, 0); }
	public TO(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TO, 0); }
	public TOUCH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TOUCH, 0); }
	public TRAILING(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRAILING, 0); }
	public TRANSACTION(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRANSACTION, 0); }
	public TRANSACTIONS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRANSACTIONS, 0); }
	public TRANSFORM(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRANSFORM, 0); }
	public TRIM(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRIM, 0); }
	public TRUE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRUE, 0); }
	public TRUNCATE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TRUNCATE, 0); }
	public TYPE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.TYPE, 0); }
	public UNARCHIVE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNARCHIVE, 0); }
	public UNBOUNDED(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNBOUNDED, 0); }
	public UNCACHE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNCACHE, 0); }
	public UNIQUE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNIQUE, 0); }
	public UNKNOWN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNKNOWN, 0); }
	public UNLOCK(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNLOCK, 0); }
	public UNSET(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UNSET, 0); }
	public UPDATE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.UPDATE, 0); }
	public USE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.USE, 0); }
	public USER(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.USER, 0); }
	public VALUES(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.VALUES, 0); }
	public VIEW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.VIEW, 0); }
	public VIEWS(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.VIEWS, 0); }
	public WHEN(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.WHEN, 0); }
	public WHERE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.WHERE, 0); }
	public WINDOW(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.WINDOW, 0); }
	public WITH(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.WITH, 0); }
	public ZONE(): TerminalNode | undefined { return this.tryGetToken(SparkSqlParser.ZONE, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return SparkSqlParser.RULE_nonReserved; }
	// @Override
	public enterRule(listener: SparkSqlListener): void {
		if (listener.enterNonReserved) {
			listener.enterNonReserved(this);
		}
	}
	// @Override
	public exitRule(listener: SparkSqlListener): void {
		if (listener.exitNonReserved) {
			listener.exitNonReserved(this);
		}
	}
	// @Override
	public accept<Result>(visitor: SparkSqlVisitor<Result>): Result {
		if (visitor.visitNonReserved) {
			return visitor.visitNonReserved(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


