package postgres

import "context"

func (db *Postgres) SetFirebaseToken(ctx context.Context, email, device, fcm string) error {
	text := `
		INSERT INTO firebase (user_id, device, token)
		SELECT id, $2, $3 
		FROM users 
		WHERE email = $1
		ON CONFLICT (user_id, device) DO UPDATE SET token = EXCLUDED.token;`

	_, err := db.pull.Exec(ctx, text, email, device, fcm)
	return err
}

func (db *Postgres) GetFirebaseToken(ctx context.Context, email string) ([]string, error) {
	text := `
		SELECT token FROM firebase
		INNER JOIN users ON firebase.user_id = users.id
		WHERE users.email = $1;`

	rows, err := db.pull.Query(ctx, text, email)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var tokens []string
	for rows.Next() {
		var token string
		if err := rows.Scan(&token); err != nil {
			return nil, err
		}
		tokens = append(tokens, token)
	}

	if err := rows.Err(); err != nil {
		return nil, err
	}

	return tokens, nil
}

func (db *Postgres) DeleteFirebaseToken(ctx context.Context, email string, badFcm []string) error {
	if len(badFcm) == 0 {
		return nil
	}
	text := `
		DELETE FROM firebase
		USING users
		WHERE firebase.user_id = users.id
		  AND users.email = $1
		  AND firebase.token = ANY($2);`

	_, err := db.pull.Exec(ctx, text, email, badFcm)
	return err
}
