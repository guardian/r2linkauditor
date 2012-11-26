package com.gu.LinkAuditor


import org.scalatest.FunSuite

class ComponentFinderTest extends FunSuite {
  test("Can filter out the velocity templates") {
    val listOfComments = "<!-- start automatic component: componentFragments/trailStrap template components/componentFragments/trailStrapDisplay.vm -->" ::
    "<!-- start automatic component: componentFragments/trailPicture template components/componentFragments/trailPictureDisplay.vm -->" ::
    "<!-- start automatic component: componentFragments/trailLinkText template components/componentFragments/trailLinkTextDisplay.vm -->" :: Nil

    val expected = "components/componentFragments/trailStrapDisplay.vm" ::
                    "components/componentFragments/trailPictureDisplay.vm" ::
                    "components/componentFragments/trailLinkTextDisplay.vm" :: Nil

    assert(expected === ComponentFinder.extractTemplates(listOfComments))

  }
}