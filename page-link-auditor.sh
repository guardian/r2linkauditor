# Audits links in a single migrated page.
#
# Args:
# 1. Old URL of the page (eg "http://www.gulocal.co.uk/music/2013/jan/20/zelenski-piano-quartet-zarebski-quintet-review")
# 2. New URL of the page (eg "http://www.thegulocal.com/music/2013/jan/20/zelenski-piano-quartet-zarebski-quintet-review")

now=$(date +%F'T'%H-%M)
out=page-link-auditor.$now.txt

echo "Starting at $(date)" > $out
echo >> $out

eval "sbt -Dsbt.log.noformat=true 'run-main com.gu.LinkAuditor.PageLinkAuditorClient $@'" >> $out 2>&1
