# Hostinger Deployment Setup

## Prerequisites
- Hostinger shared hosting account
- cPanel access
- Domain configured

## Database Setup

1. **Access cPanel MySQL Databases**
   - Login to your Hostinger cPanel
   - Navigate to "MySQL Databases"

2. **Create Database and User**
   ```
   Database Name: u831449802_remoteadmin
   Username: u831449802_malikabubakerw
   Password: 01594WainsWains
   ```

3. **Grant All Privileges**
   - Add user to database with ALL PRIVILEGES

## File Upload

1. **Prepare Files**
   ```bash
   # Compress Laravel project (exclude .git, node_modules, etc.)
   zip -r remote-admin-panel.zip web-admin-panel/ -x "*.git*" "*/node_modules/*" "*/vendor/*"
   ```

2. **Upload via File Manager**
   - Access cPanel File Manager
   - Navigate to public_html
   - Upload and extract the zip file

## Environment Configuration

1. **Create .env file**
   ```bash
   cp .env.example .env
   ```

2. **Configure .env**
   ```env
   APP_NAME="Remote Admin Panel"
   APP_ENV=production
   APP_DEBUG=false
   APP_URL=https://yourdomain.com

   DB_CONNECTION=mysql
   DB_HOST=localhost
   DB_DATABASE=u831449802_remoteadmin
   DB_USERNAME=u831449802_malikabubakerw
   DB_PASSWORD=01594WainsWains

   FIREBASE_PROJECT_ID=remoteadmin-a1089
   FIREBASE_CREDENTIALS_PATH=config/firebase-credentials.json
   ```

## Composer Dependencies

1. **Install Dependencies**
   ```bash
   # Via SSH (if available)
   composer install --no-dev --optimize-autoloader

   # Or upload vendor folder if SSH not available
   ```

2. **Generate Application Key**
   ```bash
   php artisan key:generate
   ```

## Database Migration

1. **Run Migrations**
   ```bash
   php artisan migrate --force
   ```

2. **Seed Initial Data**
   ```bash
   php artisan db:seed --class=AdminUserSeeder
   ```

## Firebase Configuration

1. **Upload Firebase Credentials**
   - Place `firebase-credentials.json` in `config/` directory
   - Ensure proper permissions (644)

2. **Test Firebase Connection**
   ```bash
   php artisan tinker
   # Test Firebase connection
   ```

## File Permissions

1. **Set Correct Permissions**
   ```bash
   chmod -R 755 storage/
   chmod -R 755 bootstrap/cache/
   chmod 644 .env
   chmod 644 config/firebase-credentials.json
   ```

## SSL/HTTPS Setup

1. **Enable SSL in Hostinger**
   - Go to SSL/TLS in cPanel
   - Enable "Let's Encrypt" SSL

2. **Force HTTPS**
   - Add to .htaccess:
   ```apache
   RewriteEngine On
   RewriteCond %{HTTPS} off
   RewriteRule ^(.*)$ https://%{HTTP_HOST}%{REQUEST_URI} [L,R=301]
   ```

## Cron Jobs (Optional)

1. **Laravel Scheduler**
   ```bash
   # Add to cPanel Cron Jobs
   * * * * * /usr/local/bin/php /home/username/public_html/artisan schedule:run >> /dev/null 2>&1
   ```

## Testing

1. **Access Admin Panel**
   ```
   URL: https://yourdomain.com
   Email: admin@example.com
   Password: admin1234
   ```

2. **Check Logs**
   ```bash
   tail -f storage/logs/laravel.log
   ```

## Troubleshooting

### Common Issues

1. **500 Internal Server Error**
   - Check file permissions
   - Verify .env configuration
   - Check error logs

2. **Database Connection Error**
   - Verify database credentials
   - Check if database exists
   - Test connection from cPanel

3. **Firebase Connection Issues**
   - Verify credentials file exists
   - Check file permissions
   - Validate JSON format

### Performance Optimization

1. **Enable OPCache**
   ```php
   opcache.enable=1
   opcache.memory_consumption=256
   opcache.max_accelerated_files=20000
   ```

2. **Configure Laravel**
   ```bash
   php artisan config:cache
   php artisan route:cache
   php artisan view:cache
   ```

## Security Checklist

- [ ] SSL certificate installed
- [ ] .env file secured (not web accessible)
- [ ] Firebase credentials secured
- [ ] Default passwords changed
- [ ] File permissions set correctly
- [ ] Error reporting disabled in production
- [ ] Database access restricted

## Backup Strategy

1. **Database Backup**
   ```bash
   mysqldump -u username -p database_name > backup.sql
   ```

2. **File Backup**
   ```bash
   tar -czf backup.tar.gz public_html/
   ```

3. **Automated Backups**
   - Use Hostinger's backup service
   - Set up automated backups via cron

## Monitoring

1. **Application Monitoring**
   - Monitor Laravel logs
   - Set up log rotation
   - Monitor Firebase quota

2. **Server Monitoring**
   - Monitor disk usage
   - Monitor database size
   - Monitor bandwidth usage