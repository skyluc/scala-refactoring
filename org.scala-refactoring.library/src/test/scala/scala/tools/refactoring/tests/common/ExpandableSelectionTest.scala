package scala.tools.refactoring.tests.common

import scala.tools.refactoring.tests.util.TestHelper
import scala.tools.refactoring.common.ReplaceableSelections
import org.junit.Assert._

class ExpandableSelectionTest extends TestHelper with ReplaceableSelections {
  import global._

  implicit class StringToSel(src: String) {
    def withSelection[T](fn: Selection => T) = {
      val tree = treeFrom(src)
      val start = commentSelectionStart(src)
      val end = commentSelectionEnd(src)
      val selection = FileSelection(tree.pos.source.file, tree, start, end)

      fn(selection)
    }
    def assertExpansion(expansion: Selection => Selection) = {
      withSelection { sel =>
        val expandedSel = expansion(sel)
        new {
          def toBecome(expectedSrc: String) = {
            expectedSrc.withSelection { expectedSel =>
              expectedSel.allSelectedTrees.zip(expandedSel.allSelectedTrees).foreach { p =>
                global.ask { () =>
                  assertEquals(p._1.toString, p._2.toString)
                }
              }
            }
          }
          
          def toRemain = toBecome(src)
        }
      }
    }

    def assertExpansionToBecome(expansion: Selection => Selection)(expectedSrc: String) = {
      withSelection { actualSel =>
        val expandedSel = expansion(actualSel)
        expectedSrc.withSelection { expectedSel =>
          expectedSel.allSelectedTrees.zip(expandedSel.allSelectedTrees).map { p =>
            global.ask { () =>
              assertEquals(p._1.toString, p._2.toString)
            }
          }
        }
      }
    }

    def assertExpansionToRemain(expansion: Selection => Selection) = {
      assertExpansionToBecome(expansion)(src)
    }
  }

  @Test
  def expandSelectionToBlock =
    """
    class C{
      def fn = {
        val a = 1
        val b = {
          val c = 2
          val d = /*(*/3/*)*/
        }
      }
    }
    """.assertExpansion(_.expandTo[Block].get).toBecome{
      """
    class C{
      def fn = {
        val a = 1
        val b = /*(*/{
          val c = 2
          val d = 3
        }/*)*/
      }
    }
    """
    }

  @Test
  def expandSelectionInForEnumerator =
    """
    class C{
      for{
        i <- List(1, 2, /*(*/3/*)*/)
      } println(i)
    }
    """.assertExpansion(_.expandTo[Tree].get).toRemain

  @Test
  def expandSelectionInYield =
    """
    class C{
      for{
        i <- List(1, 2, 3)
      } yield i * /*(*/1/*)*/
    }
    """.assertExpansion(_.expandTo[Tree].get).toRemain

  @Test
  def expandWithLastTreePartiallySelected =
    """
    class C{
      val first = 1
      /*(*/val a = 1
      val b = {
        val c = 2/*)*/
        val d = 3
        c * d
      }
      val last = 1
    }
    """.assertExpansion(_.expand.get).toBecome{
      """
    class C{
      val first = 1
      /*(*/val a = 1
      val b = {
        val c = 2
        val d = 3
        c * d
      }/*)*/
      val last = 1
    }
    """
    }

  @Test
  def expandWithFirstTreePartiallySelected =
    """
    class C{
      def fn = {
        val a = 1
        /*(*/a
      }
      val b = 2/*)*/
      val c = 3
    }
    """.assertExpansion(_.expand.get).toBecome{
      """
    class C{
      /*(*/def fn = {
        val a = 1
        a
      }
      val b = 2/*)*/
      val c = 3
    }
    """
    }
}