# Audits links recursively from a seed URL following links to a given depth,
# to check that a page and its descendants work as well in a new domain as in an old one.
#
# Args:
# 1. Old host of the page (eg "www.gulocal.co.uk")
# 2. New host of the page (eg "www.thegulocal.com")
# 3. Relative path of the page (eg "/music/2013/jan/20")
# 4. Depth of links to follow

oldUrl="http://""$1""$3"
newUrl="http://""$2""$3"
depth=$4

eval "sbt -Dsbt.log.noformat=true 'run-main com.gu.LinkAuditor.PageLinkAuditorClient $oldUrl $newUrl $depth'"
