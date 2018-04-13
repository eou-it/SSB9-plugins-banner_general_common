/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.field

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import org.hibernate.FlushMode
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.*

/**
 * Communication Field Table. Denotes a communication field in a field set. entity.
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCRCFLD")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationField.fetchByName",
                query = """ FROM CommunicationField a
                    WHERE upper(a.name) = upper(:fieldName)"""),
        @NamedQuery(name = "CommunicationField.fetchByNameForFGAC",
                query = """ select a.name FROM CommunicationField a
                    WHERE upper(a.name) = upper(:fieldName)"""),
        @NamedQuery(name = "CommunicationField.existsAnotherName",
                query = """select a.name  FROM CommunicationField a
                    WHERE  upper(a.name) = upper(:fieldName)
                    AND   a.id <> :id""")
])
class CommunicationField implements Serializable {

    /**
     * SURROGATE ID: Immutable unique key.
     */
    @Id
    @Column(name = "GCRCFLD_SURROGATE_ID")
    @SequenceGenerator(name = "GCRCFLD_SEQ_GEN", allocationSize = 1, sequenceName = "GCRCFLD_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRCFLD_SEQ_GEN")
    Long id

    /**
     * Name of the field.
     */
    @Column(name = "GCRCFLD_NAME")
    String name

    /**
     * Description of the field.
     */
    @Column(name = "GCRCFLD_DESCRIPTION")
    String description

    /**
     * FOLDER: The organizing folder this Field belongs to.
     */

    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRCFLD_FOLDER_ID", referencedColumnName = "GCRFLDR_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRCFLD_INV_GCRFLDR_KEY")
    CommunicationFolder folder

    /**
     * Indicates if the datafield was created through the seeded data set and should not be deleted or modified in any way.
     */
    @Type(type = "yes_no")
    @Column(name = "GCRCFLD_SYSTEM_IND")
    Boolean systemIndicator = false

    /**
     * FORMATSTRING: Format string with placeholders to be used.
     */
    @Lob
    @Column(name = "GCRCFLD_FORMATSTRING")
    String formatString

    /**
     * GROOVY_FORMATTER: Groovy formatter to use in place of the format string.
     */
    @Lob
    @Column(name = "GCRCFLD_GROOVY_FORMATTER")
    String groovyFormatter

    /**
     * An immutable ID that uniquely identifies this field. This maps to a RuleSetName, but intentially
     * without a hard constraint.
     */
    @Column(name = "GCRCFLD_IMMUTABLEID")
    String immutableId

    /**
     * Value to use when previewing a template that uses this field.
     */
    @Column(name = "GCRCFLD_PREVIEW_VALUE")
    String previewValue

    /**
     * Controls whether any additional escaping is done on the values when mail-merged into templates.  If true, the value will be html-escaped when mail-merged into a template content block that is an html document, if false, it will be merged as-is.
     */
    @Type(type = "yes_no")
    @Column(name = "GCRCFLD_RENDER_AS_HTML")
    Boolean renderAsHtml

    /**
     * RULE_URI: URI for the business rule (set) that provides the data to be formatted.
     */
    @Column(name = "GCRCFLD_RULE_URI")
    String ruleUri

    /**
     * State of the field. Valid values are DEVELOPMENT, PRODUCTION, and DEPRECATED.
     * Once a field leaves the DEVELOPMENT state, it may not re-enter it.
     * This is to prevent fields that are referenced elsewhere (such as templates) from being able to alter their public signature and breaking objects that reference them.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "GCRCFLD_STATUS")
    CommunicationFieldStatus status;

    /**
     * returnsArrayArguments: Indicates if the rule returns array output arguments (true or false). An array output argument is multiple rows of recurring data.
     */
    @Type(type = "yes_no")
    @Column(name = "GCRCFLD_RETURNS_ARRAY")
    Boolean returnsArrayArguments = Boolean.FALSE

    /**
     * TYPE: Type of rule content. Valid values are SQL_PREPARED_STATEMENT, SQL_CALLABLE_STATEMENT, and GROOVY_STATEMENT.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "GCRCFLD_STATEMENT_TYPE")
    CommunicationRuleStatementType statementType

    /**
     * STATEMENT: Content to be executed. May be an SQL Prepared or Callable Statement, or a Groovy script.
     */
    @Lob
    @Column(name = "GCRCFLD_RULE_STATEMENT")
    String ruleContent

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GCRCFLD_VERSION")
    Long version

    /**
     * ACTIVITY DATE: Date that record was created or last updated.
     */
    @Column(name = "GCRCFLD_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCRCFLD_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCRCFLD_DATA_ORIGIN")
    String dataOrigin

    /**
     * Holds the value of the next available parameter key.
     */

    @Transient
    private int nextParameterKey;

    /**
     * Contains a copy of fields original state to support validation methods that require a comparison of original
     * and proposed state to determine what changed and whether the changes are valid at that point in the object's
     * lifecycle.
     */

    @Transient
    private CommunicationFieldStatus originalStatus = null;
    @Transient
    private List<Integer> originalParameterKeys = new ArrayList<Integer>();


    static constraints = {
        name(nullable: false, maxSize: 255)
        description(nullable: true, maxSize: 2000)
        systemIndicator(nullable:false)
        folder(nullable: false)
        formatString(nullable: true)
        groovyFormatter(nullable: true)
        /*
        immutableId is actually not null, but is set nullable so it passes domain validation and is populated by the service
         */
        immutableId(nullable: true, maxSize: 144)
        previewValue(nullable: true, maxSize: 1020)
        renderAsHtml(nullable: false)
        ruleUri(nullable: true, maxSize: 1020)
        /*
        status is actually not null, but is set nullable so it passes domain validation and
        is populated by the service preCreate and throws an exception if null on preUpdate
         */
        status(nullable: true, maxSize: 100)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        /*
        If any of these three are not null, the others must be not null. This is validated in the service
        */
        returnsArrayArguments(nullable: true)
        statementType(nullable: true)
        ruleContent(nullable: true)
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id', 'immutableId']

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'
        def queryCriteria = CommunicationField.createCriteria()

        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            createAlias("folder", "folder")
            ilike("name", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name))
            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }


    public static CommunicationField fetchByName(String fieldName) {

        def query
        CommunicationField.withSession { session ->
            query = session.getNamedQuery('CommunicationField.fetchByName')
                    .setString('fieldName', fieldName)
                    .list()[0]
        }
        return query
    }

    public static Boolean fetchByNameForFGAC(String fieldName) {

        def query
        CommunicationField.withSession { session ->
            query = session.getNamedQuery('CommunicationField.fetchByNameForFGAC')
                    .setString('fieldName', fieldName)
                    .list()[0]
        }
        return (query != null)
    }


    public static Boolean existsAnotherName(Long fieldId, String fieldName) {

        def query
        CommunicationField.withSession { session ->
            session.setFlushMode(FlushMode.MANUAL);
            try {
                query = session.getNamedQuery('CommunicationField.existsAnotherName')
                        .setString('fieldName', fieldName)
                        .setLong('id', fieldId).list()[0]
            } finally {
                session.setFlushMode(FlushMode.AUTO)
            }
        }
        return (query != null)
    }

    public boolean isPublished() {
        return !CommunicationFieldStatus.DEVELOPMENT.equals( this.getStatus() )
    }
}
