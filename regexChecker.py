import re
import unittest

class TestRegexForLinkChecker(unittest.TestCase):
    # linkchecker only has a "blacklist" option which can be expressed as a Regex
    # --ignore-url=REGEX
    # however, we would like to constrain the site spider to GUARDIAN urls only.


    def setUp(self):
        self.r = re.compile("^((?!\.guardian\.).)*$")

    def test_should_not_match_Guardian_co_uk(self):
        self.assertFalse(self.r.match("http://www.guardian.co.uk/money/for/nothing"))

    def test_should_not_match_theguardian_com(self):
        self.assertFalse(self.r.match("http://www.theguardian.com/money/for/nothing"))

    def test_should_not_match_guardian_co_uk_subdomain(self):
        self.assertFalse(self.r.match("http://tvlistings/guardian.co.uk/?INTCMP=ELKJF"))

    def test_should_not_match_gu_com(self):
        self.assertFalse(self.r.match("http://"))

    def test_should_match_non_Guardian_sites(self):
        self.assertFalse(self.r.match("http://pixel.quantserve.com/pixel/blah"))
        self.assertFalse(self.r.match("http://www.guardianbookshop.co.uk/alfkdjklfjafd"))


if __name__ == '__main__':
    unittest.main()
