// Generated from /home/fred/.boot/cache/tmp/home/fred/projects/fql/src/catdata/aql/grammar/5r0/uanrg/AqlParser.g4 by ANTLR 4.7
package catdata.aql.grammar.target;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link AqlParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface AqlParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link AqlParser#file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFile(AqlParser.FileContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#symbol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSymbol(AqlParser.SymbolContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(AqlParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#optionsDeclarationSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptionsDeclarationSection(AqlParser.OptionsDeclarationSectionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Comment_HTML}
	 * labeled alternative in {@link AqlParser#commentDeclarationSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment_HTML(AqlParser.Comment_HTMLContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Comment_MD}
	 * labeled alternative in {@link AqlParser#commentDeclarationSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment_MD(AqlParser.Comment_MDContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#kindDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKindDeclaration(AqlParser.KindDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#path}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath(AqlParser.PathContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pathNodeId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathNodeId(AqlParser.PathNodeIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(AqlParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#htmlCommentDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlCommentDeclaration(AqlParser.HtmlCommentDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mdCommentDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMdCommentDeclaration(AqlParser.MdCommentDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#allOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllOptions(AqlParser.AllOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#optionsDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptionsDeclaration(AqlParser.OptionsDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#importJoinedOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportJoinedOption(AqlParser.ImportJoinedOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#completionPresedenceOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompletionPresedenceOption(AqlParser.CompletionPresedenceOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mapNullsArbitrarilyUnsafeOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapNullsArbitrarilyUnsafeOption(AqlParser.MapNullsArbitrarilyUnsafeOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#interpretAsAlgebraOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterpretAsAlgebraOption(AqlParser.InterpretAsAlgebraOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#numThreadsOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumThreadsOption(AqlParser.NumThreadsOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#randomSeedOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRandomSeedOption(AqlParser.RandomSeedOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#timeoutOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeoutOption(AqlParser.TimeoutOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#requireConsistencyOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRequireConsistencyOption(AqlParser.RequireConsistencyOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaOnlyOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaOnlyOption(AqlParser.SchemaOnlyOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#allowJavaEqsUnsafeOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllowJavaEqsUnsafeOption(AqlParser.AllowJavaEqsUnsafeOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#dontValidateUnsafeOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDontValidateUnsafeOption(AqlParser.DontValidateUnsafeOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#alwaysReloadOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlwaysReloadOption(AqlParser.AlwaysReloadOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryComposeUseIncomplete}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryComposeUseIncomplete(AqlParser.QueryComposeUseIncompleteContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#csvOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCsvOptions(AqlParser.CsvOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#idColumnNameOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdColumnNameOption(AqlParser.IdColumnNameOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#varcharLengthOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarcharLengthOption(AqlParser.VarcharLengthOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#startIdsAtOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStartIdsAtOption(AqlParser.StartIdsAtOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#importAsTheoryOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportAsTheoryOption(AqlParser.ImportAsTheoryOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#jdbcDefaultClassOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJdbcDefaultClassOption(AqlParser.JdbcDefaultClassOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#jdbDefaultStringOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJdbDefaultStringOption(AqlParser.JdbDefaultStringOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#dVIAFProverUnsafeOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDVIAFProverUnsafeOption(AqlParser.DVIAFProverUnsafeOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#proverOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProverOptions(AqlParser.ProverOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#guiOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGuiOptions(AqlParser.GuiOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#evalOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvalOptions(AqlParser.EvalOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#coproductOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCoproductOptions(AqlParser.CoproductOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryRemoveRedundancyOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryRemoveRedundancyOption(AqlParser.QueryRemoveRedundancyOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#truthy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTruthy(AqlParser.TruthyContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#proverType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProverType(AqlParser.ProverTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideId(AqlParser.TypesideIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideKindAssignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideKindAssignment(AqlParser.TypesideKindAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideDef(AqlParser.TypesideDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideKind(AqlParser.TypesideKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideLiteralSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideLiteralSection(AqlParser.TypesideLiteralSectionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Typeside_ImportName}
	 * labeled alternative in {@link AqlParser#typesideImport}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeside_ImportName(AqlParser.Typeside_ImportNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideTypeSig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideTypeSig(AqlParser.TypesideTypeSigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideJavaTypeSig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideJavaTypeSig(AqlParser.TypesideJavaTypeSigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideTypeId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideTypeId(AqlParser.TypesideTypeIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideConstantSig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideConstantSig(AqlParser.TypesideConstantSigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideConstantValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideConstantValue(AqlParser.TypesideConstantValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideJavaConstantSig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideJavaConstantSig(AqlParser.TypesideJavaConstantSigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideConstantLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideConstantLiteral(AqlParser.TypesideConstantLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideFunctionSig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideFunctionSig(AqlParser.TypesideFunctionSigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideFnLocal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideFnLocal(AqlParser.TypesideFnLocalContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideJavaFunctionSig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideJavaFunctionSig(AqlParser.TypesideJavaFunctionSigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideFnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideFnName(AqlParser.TypesideFnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideEquationSig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideEquationSig(AqlParser.TypesideEquationSigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideLocal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideLocal(AqlParser.TypesideLocalContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideLocalType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideLocalType(AqlParser.TypesideLocalTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Typeside_EvalNumber}
	 * labeled alternative in {@link AqlParser#typesideEval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeside_EvalNumber(AqlParser.Typeside_EvalNumberContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Typeside_EvalGen}
	 * labeled alternative in {@link AqlParser#typesideEval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeside_EvalGen(AqlParser.Typeside_EvalGenContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Typeside_InfixFunction}
	 * labeled alternative in {@link AqlParser#typesideEval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeside_InfixFunction(AqlParser.Typeside_InfixFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Typeside_EvalFunction}
	 * labeled alternative in {@link AqlParser#typesideEval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeside_EvalFunction(AqlParser.Typeside_EvalFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#typesideLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesideLiteral(AqlParser.TypesideLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaId(AqlParser.SchemaIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaKindAssignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaKindAssignment(AqlParser.SchemaKindAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Schema_Empty}
	 * labeled alternative in {@link AqlParser#schemaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_Empty(AqlParser.Schema_EmptyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Schema_OfInstance}
	 * labeled alternative in {@link AqlParser#schemaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_OfInstance(AqlParser.Schema_OfInstanceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Schema_Destination}
	 * labeled alternative in {@link AqlParser#schemaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_Destination(AqlParser.Schema_DestinationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Schema_Literal}
	 * labeled alternative in {@link AqlParser#schemaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_Literal(AqlParser.Schema_LiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Schema_GetSchemaColimit}
	 * labeled alternative in {@link AqlParser#schemaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_GetSchemaColimit(AqlParser.Schema_GetSchemaColimitContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaKind(AqlParser.SchemaKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaColimitId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaColimitId(AqlParser.SchemaColimitIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaLiteralSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaLiteralSection(AqlParser.SchemaLiteralSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaEntityId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaEntityId(AqlParser.SchemaEntityIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaForeignSig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaForeignSig(AqlParser.SchemaForeignSigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaPathEquation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaPathEquation(AqlParser.SchemaPathEquationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaPath(AqlParser.SchemaPathContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaArrowId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaArrowId(AqlParser.SchemaArrowIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaTermId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaTermId(AqlParser.SchemaTermIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaAttributeSig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaAttributeSig(AqlParser.SchemaAttributeSigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaAttributeId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaAttributeId(AqlParser.SchemaAttributeIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaObservationEquationSig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaObservationEquationSig(AqlParser.SchemaObservationEquationSigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaEquationSig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaEquationSig(AqlParser.SchemaEquationSigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#evalSchemaFn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvalSchemaFn(AqlParser.EvalSchemaFnContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaGen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaGen(AqlParser.SchemaGenContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaGenType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaGenType(AqlParser.SchemaGenTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaFn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaFn(AqlParser.SchemaFnContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaForeignId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaForeignId(AqlParser.SchemaForeignIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaLiteralValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaLiteralValue(AqlParser.SchemaLiteralValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceId(AqlParser.InstanceIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceKindAssignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceKindAssignment(AqlParser.InstanceKindAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_Empty}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_Empty(AqlParser.Instance_EmptyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_Src}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_Src(AqlParser.Instance_SrcContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_Dst}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_Dst(AqlParser.Instance_DstContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_Distinct}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_Distinct(AqlParser.Instance_DistinctContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_Eval}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_Eval(AqlParser.Instance_EvalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_Coeval}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_Coeval(AqlParser.Instance_CoevalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_Delta}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_Delta(AqlParser.Instance_DeltaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_Sigma}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_Sigma(AqlParser.Instance_SigmaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_CoSigma}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_CoSigma(AqlParser.Instance_CoSigmaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_Coprod}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_Coprod(AqlParser.Instance_CoprodContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_CoprodUn}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_CoprodUn(AqlParser.Instance_CoprodUnContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_CoEqual}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_CoEqual(AqlParser.Instance_CoEqualContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_CoLimit}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_CoLimit(AqlParser.Instance_CoLimitContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_ImportJdbc}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_ImportJdbc(AqlParser.Instance_ImportJdbcContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_QuotientJdbc}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_QuotientJdbc(AqlParser.Instance_QuotientJdbcContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_QuotientCsv}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_QuotientCsv(AqlParser.Instance_QuotientCsvContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_ImportJdbcAll}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_ImportJdbcAll(AqlParser.Instance_ImportJdbcAllContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_ImportCsv}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_ImportCsv(AqlParser.Instance_ImportCsvContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_Literal}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_Literal(AqlParser.Instance_LiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_Quotient}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_Quotient(AqlParser.Instance_QuotientContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_Chase}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_Chase(AqlParser.Instance_ChaseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Instance_Random}
	 * labeled alternative in {@link AqlParser#instanceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstance_Random(AqlParser.Instance_RandomContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceKind(AqlParser.InstanceKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceImportJdbcAllSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceImportJdbcAllSection(AqlParser.InstanceImportJdbcAllSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceColimitSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceColimitSection(AqlParser.InstanceColimitSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceLiteralSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceLiteralSection(AqlParser.InstanceLiteralSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceImportJdbcSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceImportJdbcSection(AqlParser.InstanceImportJdbcSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#jdbcClass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJdbcClass(AqlParser.JdbcClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#jdbcUri}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJdbcUri(AqlParser.JdbcUriContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceSql}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceSql(AqlParser.InstanceSqlContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceQuotientCsvSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceQuotientCsvSection(AqlParser.InstanceQuotientCsvSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceFile}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceFile(AqlParser.InstanceFileContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceGen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceGen(AqlParser.InstanceGenContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceEquation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceEquation(AqlParser.InstanceEquationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceMultiEquation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceMultiEquation(AqlParser.InstanceMultiEquationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceEquationId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceEquationId(AqlParser.InstanceEquationIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceMultiBind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceMultiBind(AqlParser.InstanceMultiBindContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceSymbol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceSymbol(AqlParser.InstanceSymbolContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceLiteral(AqlParser.InstanceLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceLiteralValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceLiteralValue(AqlParser.InstanceLiteralValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instancePath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstancePath(AqlParser.InstancePathContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceArrowId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceArrowId(AqlParser.InstanceArrowIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceQuotientJdbcSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceQuotientJdbcSection(AqlParser.InstanceQuotientJdbcSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceQuotientSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceQuotientSection(AqlParser.InstanceQuotientSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceRandomSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceRandomSection(AqlParser.InstanceRandomSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceEvalSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceEvalSection(AqlParser.InstanceEvalSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceCoevalSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceCoevalSection(AqlParser.InstanceCoevalSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceSigmaSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceSigmaSection(AqlParser.InstanceSigmaSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceCoprodSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceCoprodSection(AqlParser.InstanceCoprodSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceCoprodSigmaSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceCoprodSigmaSection(AqlParser.InstanceCoprodSigmaSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceCoprodUnrestrictSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceCoprodUnrestrictSection(AqlParser.InstanceCoprodUnrestrictSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceCoequalizeSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceCoequalizeSection(AqlParser.InstanceCoequalizeSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceImportCsvSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceImportCsvSection(AqlParser.InstanceImportCsvSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#instanceCsvId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceCsvId(AqlParser.InstanceCsvIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mappingId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMappingId(AqlParser.MappingIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mappingKindAssignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMappingKindAssignment(AqlParser.MappingKindAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MapExp_Id}
	 * labeled alternative in {@link AqlParser#mappingDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapExp_Id(AqlParser.MapExp_IdContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MapExp_Compose}
	 * labeled alternative in {@link AqlParser#mappingDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapExp_Compose(AqlParser.MapExp_ComposeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MapExp_Literal}
	 * labeled alternative in {@link AqlParser#mappingDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapExp_Literal(AqlParser.MapExp_LiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MapExp_Get}
	 * labeled alternative in {@link AqlParser#mappingDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapExp_Get(AqlParser.MapExp_GetContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mappingKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMappingKind(AqlParser.MappingKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mappingLiteralSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMappingLiteralSection(AqlParser.MappingLiteralSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mappingEntitySig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMappingEntitySig(AqlParser.MappingEntitySigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mappingForeignSig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMappingForeignSig(AqlParser.MappingForeignSigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mappingForeignPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMappingForeignPath(AqlParser.MappingForeignPathContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mappingArrowId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMappingArrowId(AqlParser.MappingArrowIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mappingAttributeSig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMappingAttributeSig(AqlParser.MappingAttributeSigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mappingLambda}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMappingLambda(AqlParser.MappingLambdaContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mappingGen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMappingGen(AqlParser.MappingGenContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mappingGenType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMappingGenType(AqlParser.MappingGenTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#evalMappingFn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvalMappingFn(AqlParser.EvalMappingFnContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#mappingFn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMappingFn(AqlParser.MappingFnContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformId(AqlParser.TransformIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformKindAssignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformKindAssignment(AqlParser.TransformKindAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Transform_Id}
	 * labeled alternative in {@link AqlParser#transformDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_Id(AqlParser.Transform_IdContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Transform_Compose}
	 * labeled alternative in {@link AqlParser#transformDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_Compose(AqlParser.Transform_ComposeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Transform_Destination}
	 * labeled alternative in {@link AqlParser#transformDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_Destination(AqlParser.Transform_DestinationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Transform_Delta}
	 * labeled alternative in {@link AqlParser#transformDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_Delta(AqlParser.Transform_DeltaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Transform_Sigma}
	 * labeled alternative in {@link AqlParser#transformDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_Sigma(AqlParser.Transform_SigmaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Transform_Eval}
	 * labeled alternative in {@link AqlParser#transformDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_Eval(AqlParser.Transform_EvalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Transform_Coeval}
	 * labeled alternative in {@link AqlParser#transformDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_Coeval(AqlParser.Transform_CoevalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Transform_Unit}
	 * labeled alternative in {@link AqlParser#transformDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_Unit(AqlParser.Transform_UnitContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Transform_Counit}
	 * labeled alternative in {@link AqlParser#transformDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_Counit(AqlParser.Transform_CounitContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Transform_UnitQuery}
	 * labeled alternative in {@link AqlParser#transformDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_UnitQuery(AqlParser.Transform_UnitQueryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Transform_CounitQuery}
	 * labeled alternative in {@link AqlParser#transformDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_CounitQuery(AqlParser.Transform_CounitQueryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Transform_ImportJdbc}
	 * labeled alternative in {@link AqlParser#transformDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_ImportJdbc(AqlParser.Transform_ImportJdbcContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Transform_ImportCsv}
	 * labeled alternative in {@link AqlParser#transformDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_ImportCsv(AqlParser.Transform_ImportCsvContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Transform_Literal}
	 * labeled alternative in {@link AqlParser#transformDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_Literal(AqlParser.Transform_LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformKind(AqlParser.TransformKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformJdbcClass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformJdbcClass(AqlParser.TransformJdbcClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformJdbcUri}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformJdbcUri(AqlParser.TransformJdbcUriContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformFile}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformFile(AqlParser.TransformFileContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformSqlExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformSqlExpr(AqlParser.TransformSqlExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformSigmaSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformSigmaSection(AqlParser.TransformSigmaSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformCoevalSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformCoevalSection(AqlParser.TransformCoevalSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformUnitSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformUnitSection(AqlParser.TransformUnitSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformUnitQuerySection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformUnitQuerySection(AqlParser.TransformUnitQuerySectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformCounitQuerySection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformCounitQuerySection(AqlParser.TransformCounitQuerySectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformImportJdbcSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformImportJdbcSection(AqlParser.TransformImportJdbcSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformImportCsvSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformImportCsvSection(AqlParser.TransformImportCsvSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformSqlEntityExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformSqlEntityExpr(AqlParser.TransformSqlEntityExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformFileExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformFileExpr(AqlParser.TransformFileExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformLiteralSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformLiteralSection(AqlParser.TransformLiteralSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#transformGen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformGen(AqlParser.TransformGenContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryId(AqlParser.QueryIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryFromSchema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryFromSchema(AqlParser.QueryFromSchemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryKindAssignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryKindAssignment(AqlParser.QueryKindAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code QueryExp_Id}
	 * labeled alternative in {@link AqlParser#queryDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryExp_Id(AqlParser.QueryExp_IdContext ctx);
	/**
	 * Visit a parse tree produced by the {@code QueryExp_Literal}
	 * labeled alternative in {@link AqlParser#queryDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryExp_Literal(AqlParser.QueryExp_LiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code QueryExp_Simple}
	 * labeled alternative in {@link AqlParser#queryDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryExp_Simple(AqlParser.QueryExp_SimpleContext ctx);
	/**
	 * Visit a parse tree produced by the {@code QueryExp_Get}
	 * labeled alternative in {@link AqlParser#queryDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryExp_Get(AqlParser.QueryExp_GetContext ctx);
	/**
	 * Visit a parse tree produced by the {@code QueryExp_FromMapping}
	 * labeled alternative in {@link AqlParser#queryDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryExp_FromMapping(AqlParser.QueryExp_FromMappingContext ctx);
	/**
	 * Visit a parse tree produced by the {@code QueryExp_Composition}
	 * labeled alternative in {@link AqlParser#queryDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryExp_Composition(AqlParser.QueryExp_CompositionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryKind(AqlParser.QueryKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryLiteralSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryLiteralSection(AqlParser.QueryLiteralSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryEntityExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryEntityExpr(AqlParser.QueryEntityExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#querySimpleSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuerySimpleSection(AqlParser.QuerySimpleSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryLiteralValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryLiteralValue(AqlParser.QueryLiteralValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryClauseExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryClauseExpr(AqlParser.QueryClauseExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryForeignSig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryForeignSig(AqlParser.QueryForeignSigContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryPathMapping}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryPathMapping(AqlParser.QueryPathMappingContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryGen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryGen(AqlParser.QueryGenContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryPath(AqlParser.QueryPathContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryFromMappingSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryFromMappingSection(AqlParser.QueryFromMappingSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryFromSchemaSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryFromSchemaSection(AqlParser.QueryFromSchemaSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#graphId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraphId(AqlParser.GraphIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#graphKindAssignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraphKindAssignment(AqlParser.GraphKindAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code GraphExp_Literal}
	 * labeled alternative in {@link AqlParser#graphDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraphExp_Literal(AqlParser.GraphExp_LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#graphKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraphKind(AqlParser.GraphKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#graphLiteralSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraphLiteralSection(AqlParser.GraphLiteralSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#graphNodeId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraphNodeId(AqlParser.GraphNodeIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#graphEdgeId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraphEdgeId(AqlParser.GraphEdgeIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaId(AqlParser.PragmaIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaKindAssignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaKindAssignment(AqlParser.PragmaKindAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Pragma_CmdLine}
	 * labeled alternative in {@link AqlParser#pragmaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma_CmdLine(AqlParser.Pragma_CmdLineContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Pragma_ExecJs}
	 * labeled alternative in {@link AqlParser#pragmaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma_ExecJs(AqlParser.Pragma_ExecJsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Pragma_ExecJdbc}
	 * labeled alternative in {@link AqlParser#pragmaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma_ExecJdbc(AqlParser.Pragma_ExecJdbcContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Pragma_Check}
	 * labeled alternative in {@link AqlParser#pragmaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma_Check(AqlParser.Pragma_CheckContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Pragma_AssertConsistent}
	 * labeled alternative in {@link AqlParser#pragmaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma_AssertConsistent(AqlParser.Pragma_AssertConsistentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Pragma_ExportCsvInstance}
	 * labeled alternative in {@link AqlParser#pragmaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma_ExportCsvInstance(AqlParser.Pragma_ExportCsvInstanceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Pragma_ExportCsvTransform}
	 * labeled alternative in {@link AqlParser#pragmaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma_ExportCsvTransform(AqlParser.Pragma_ExportCsvTransformContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Pragma_ExportJdbcInstance}
	 * labeled alternative in {@link AqlParser#pragmaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma_ExportJdbcInstance(AqlParser.Pragma_ExportJdbcInstanceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Pragma_ExportJdbcQuery}
	 * labeled alternative in {@link AqlParser#pragmaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma_ExportJdbcQuery(AqlParser.Pragma_ExportJdbcQueryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Pragma_ExportJdbcTransform}
	 * labeled alternative in {@link AqlParser#pragmaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma_ExportJdbcTransform(AqlParser.Pragma_ExportJdbcTransformContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Pragma_AddToClasspath}
	 * labeled alternative in {@link AqlParser#pragmaDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma_AddToClasspath(AqlParser.Pragma_AddToClasspathContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaKind(AqlParser.PragmaKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaAddClasspathSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaAddClasspathSection(AqlParser.PragmaAddClasspathSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaCmdLineSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaCmdLineSection(AqlParser.PragmaCmdLineSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaExecJsSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaExecJsSection(AqlParser.PragmaExecJsSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaExecJdbcSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaExecJdbcSection(AqlParser.PragmaExecJdbcSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaExportCsvSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaExportCsvSection(AqlParser.PragmaExportCsvSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaExportJdbcSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaExportJdbcSection(AqlParser.PragmaExportJdbcSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaFile}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaFile(AqlParser.PragmaFileContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaJdbcClass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaJdbcClass(AqlParser.PragmaJdbcClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaJdbcUri}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaJdbcUri(AqlParser.PragmaJdbcUriContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaPrefix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaPrefix(AqlParser.PragmaPrefixContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaPrefixSrc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaPrefixSrc(AqlParser.PragmaPrefixSrcContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pragmaPrefixDst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragmaPrefixDst(AqlParser.PragmaPrefixDstContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaColimitKindAssignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaColimitKindAssignment(AqlParser.SchemaColimitKindAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SchemaColimit_Quotient}
	 * labeled alternative in {@link AqlParser#schemaColimitDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaColimit_Quotient(AqlParser.SchemaColimit_QuotientContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SchemaColimit_Coproduct}
	 * labeled alternative in {@link AqlParser#schemaColimitDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaColimit_Coproduct(AqlParser.SchemaColimit_CoproductContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SchemaColimit_Modify}
	 * labeled alternative in {@link AqlParser#schemaColimitDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaColimit_Modify(AqlParser.SchemaColimit_ModifyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SchemaColimit_Wrap}
	 * labeled alternative in {@link AqlParser#schemaColimitDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaColimit_Wrap(AqlParser.SchemaColimit_WrapContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaColimitKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaColimitKind(AqlParser.SchemaColimitKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaColimitQuotientSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaColimitQuotientSection(AqlParser.SchemaColimitQuotientSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#scObsEquation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScObsEquation(AqlParser.ScObsEquationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#scGen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScGen(AqlParser.ScGenContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#scEntityPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScEntityPath(AqlParser.ScEntityPathContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#scFkPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScFkPath(AqlParser.ScFkPathContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#scAttrPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScAttrPath(AqlParser.ScAttrPathContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#schemaColimitModifySection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaColimitModifySection(AqlParser.SchemaColimitModifySectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#constraintId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintId(AqlParser.ConstraintIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#constraintKindAssignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintKindAssignment(AqlParser.ConstraintKindAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ConstraintExp_Literal}
	 * labeled alternative in {@link AqlParser#constraintDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintExp_Literal(AqlParser.ConstraintExp_LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#constraintKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintKind(AqlParser.ConstraintKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#constraintLiteralSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintLiteralSection(AqlParser.ConstraintLiteralSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#constraintExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintExpr(AqlParser.ConstraintExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#constraintGen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintGen(AqlParser.ConstraintGenContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#constraintEquation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintEquation(AqlParser.ConstraintEquationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#constraintPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintPath(AqlParser.ConstraintPathContext ctx);
}