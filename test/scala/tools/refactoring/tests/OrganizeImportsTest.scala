package scala.tools.refactoring.tests

import scala.tools.refactoring.Refactoring
import scala.tools.refactoring.tests.util.TestRefactoring
import scala.tools.refactoring.common.Tracing
import scala.tools.refactoring.common.SilentTracing
import scala.tools.refactoring.OrganizeImports
import scala.tools.refactoring.tests.util.TestHelper
import org.junit.Test

class OrganizeImportsTest extends TestHelper with TestRefactoring {

  implicit def stringToRefactoring(src: String) = new TestRefactoringImpl(src, "test") {
    val refactoring = new OrganizeImports(global) with /*Silent*/Tracing
    def organize(e: String) = doIt(e, new refactoring.RefactoringParameters)
  }

  @Test
  def sort = """
    import scala.collection.mutable.ListBuffer
    import java.lang.Object

    object Main
    """ organize(
    """
    import java.lang.Object
    import scala.collection.mutable.ListBuffer

    object Main
    """)
    
  @Test
  def collapse = """
    import java.lang.String
    import java.lang.Object

    object Main
    """ organize(
    """
    import java.lang.{Object, String}

    object Main
    """)    
    
  @Test
  def sortAndCollapse = """
    import scala.collection.mutable.ListBuffer
    import java.lang.String
    import java.lang.Object

    object Main
    """ organize(
    """
    import java.lang.{Object, String}
    import scala.collection.mutable.ListBuffer

    object Main
    """)    
    
  @Test
  def collapseWithRename = """
    import java.lang.{String => S}
    import java.lang.{Object => O}

    object Main
    """ organize(
    """
    import java.lang.{Object => O, String => S}

    object Main
    """)     
    
  @Test
  def importAll = """
    import java.lang._
    import java.lang.String

    object Main
    """ organize(
    """
    import java.lang._

    object Main
    """)      
    
  @Test
  def importOnTrait = """
    import java.lang._
    import java.lang.String

    trait A

    trait Main extends A {
    }
    """ organize(
    """
    import java.lang._

    trait A

    trait Main extends A {
    }
    """)    
    
  @Test
  def importWithSpace = """

    import scala.collection.mutable.ListBuffer
    import java.lang.String

    object Main
    """ organize(
    """

    import java.lang.String
    import scala.collection.mutable.ListBuffer

    object Main
    """)    
    
  @Test
  def importAllWithRename = """
    import java.lang._
    import java.lang.{String => S}

    object Main
    """ organize(
    """
    import java.lang.{String => S, _}

    object Main
    """)
}