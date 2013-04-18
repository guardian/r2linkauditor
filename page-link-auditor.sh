# Audits links in a single migrated page.
#
# Args:
# 1. Old host of the page (eg "www.gulocal.co.uk")
# 2. New host of the page (eg "www.thegulocal.com")
# 3. Relative path of the page (eg "/music/2013/jan/20")

oldUrl="http://""$1""$3"
newUrl="http://""$2""$3"

eval "sbt -Dsbt.log.noformat=true 'run-main com.gu.LinkAuditor.PageLinkAuditorClient $oldUrl $newUrl'"
