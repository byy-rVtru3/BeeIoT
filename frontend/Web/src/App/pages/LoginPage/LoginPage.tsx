import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  IconButton,
  InputAdornment,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { z } from 'zod';

import EyeIcon from '@/App/components/icon/EyeIcon';
import HiveLogo from '@/App/components/icon/HiveLogo';
import { useSignInMutation } from '@/hooks/mutations/useSignInMutation';
import { useAuthStore } from '@/store/useAuthStore';

const loginSchema = z.object({
  email: z.string().min(1, 'Введите email').email('Некорректный email'),
  password: z.string().min(1, 'Введите пароль'),
});

type LoginFormData = z.infer<typeof loginSchema>;

const getLoginErrorMessage = (error: unknown) => {
  const maybeError = error as { response?: { data?: unknown; status?: number } };
  const data = maybeError.response?.data;
  if (typeof data === 'string' && data.trim()) {
    return data;
  }
  if (data && typeof data === 'object') {
    const message = (data as { message?: unknown }).message;
    if (typeof message === 'string' && message.trim()) {
      return message;
    }
  }
  if (maybeError.response?.status) {
    return `Не удалось войти (код ${maybeError.response.status})`;
  }
  return 'Не удалось войти. Проверьте данные и повторите.';
};

const LoginPage = () => {
  const signIn = useAuthStore((state) => state.signIn);
  const navigate = useNavigate();
  const [showPwd, setShowPwd] = useState(false);
  const isDev = import.meta.env.DEV;

  const {
    register,
    handleSubmit,
    setError,
    clearErrors,
    formState: { errors, isValid },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: 'admin@beeiot.app',
      password: '',
    },
    mode: 'onChange',
  });

  const signInMutation = useSignInMutation({
    onSuccess: (response, variables) => {
      signIn({ email: variables.email }, response.data.token);
      navigate('/admin/description', { replace: true });
    },
    onError: (error) => {
      setError('root', { message: getLoginErrorMessage(error) });
    },
  });

  const onSubmit = (values: LoginFormData) => {
    clearErrors('root');
    signInMutation.mutate(values);
  };

  const handleDevLogin = () => {
    signIn({ email: 'dev@beeiot.app' }, 'dev-token');
    navigate('/admin/description', { replace: true });
  };

  return (
    <Box
      sx={{
        height: '100dvh',
        minHeight: '100vh',
        width: '100%',
        boxSizing: 'border-box',
        backgroundColor: 'rgb(244,244,244)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        p: 2,
        backgroundImage: 'radial-gradient(rgba(255,182,39,0.07) 1.5px, transparent 1.5px)',
        backgroundSize: '22px 22px',
      }}
    >
      <Paper
        elevation={0}
        sx={{
          width: '100%',
          maxWidth: 420,
          backgroundColor: '#fff',
          borderRadius: '20px',
          border: '1px solid rgb(255,182,39)',
          boxShadow: '0px 0px 24px rgba(0,0,0,0.08)',
          p: { xs: 3.5, sm: 5 },
        }}
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3.5 }}>
          <HiveLogo />
          <Typography variant="h4" sx={{ mt: 2, fontWeight: 700, fontSize: 28 }}>
            BeeIoT Admin
          </Typography>
          <Typography variant="body2" sx={{ color: 'rgba(0,0,0,0.55)', mt: 0.5 }}>
            Вход в панель управления
          </Typography>
        </Box>

        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <Box>
              <Typography
                variant="body2"
                sx={{ mb: 0.75, fontWeight: 500, color: 'rgba(0,0,0,0.7)' }}
              >
                Email
              </Typography>
              <TextField
                fullWidth
                size="medium"
                placeholder="admin@example.com"
                autoComplete="email"
                error={Boolean(errors.email)}
                helperText={errors.email?.message ?? ' '}
                {...register('email')}
              />
            </Box>

            <Box>
              <Typography
                variant="body2"
                sx={{ mb: 0.75, fontWeight: 500, color: 'rgba(0,0,0,0.7)' }}
              >
                Пароль
              </Typography>
              <TextField
                fullWidth
                size="medium"
                type={showPwd ? 'text' : 'password'}
                placeholder="••••••••"
                autoComplete="current-password"
                error={Boolean(errors.password)}
                helperText={errors.password?.message ?? ' '}
                slotProps={{
                  input: {
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          onClick={() => setShowPwd((current) => !current)}
                          edge="end"
                          size="small"
                        >
                          <EyeIcon off={!showPwd} />
                        </IconButton>
                      </InputAdornment>
                    ),
                  },
                }}
                {...register('password')}
              />
            </Box>

            <Button
              type="submit"
              variant="outlined"
              size="large"
              disabled={signInMutation.isPending || !isValid}
              sx={{
                mt: 1,
                fontSize: 16,
                py: 1.4,
                borderRadius: '10px',
                borderWidth: '2px',
                borderColor: 'rgb(255,182,39)',
                bgcolor: '#fff',
                color: '#000',
                fontWeight: 600,
                '&:hover': {
                  borderWidth: '2px',
                  borderColor: 'rgb(228,147,19)',
                  bgcolor: 'rgba(255,182,39,0.12)',
                },
                '&.Mui-disabled': {
                  borderWidth: '2px',
                  borderColor: 'rgba(0,0,0,0.12)',
                  bgcolor: 'rgba(0,0,0,0.03)',
                },
              }}
            >
              {signInMutation.isPending ? (
                <CircularProgress size={22} sx={{ color: '#000' }} />
              ) : (
                'Войти'
              )}
            </Button>
            {isDev ? (
              <Button
                type="button"
                variant="text"
                size="small"
                onClick={handleDevLogin}
                sx={{
                  alignSelf: 'flex-start',
                  color: 'rgba(0,0,0,0.55)',
                  textTransform: 'none',
                  px: 0,
                  '&:hover': {
                    color: '#000',
                    bgcolor: 'transparent',
                  },
                }}
              >
                Dev-вход без сервера
              </Button>
            ) : null}
            {errors.root?.message ? (
              <Alert severity="error" sx={{ borderRadius: 2 }}>
                {errors.root.message}
              </Alert>
            ) : null}
          </Box>
        </form>

        <Typography
          variant="caption"
          sx={{
            display: 'block',
            textAlign: 'center',
            mt: 3,
            color: 'rgba(0,0,0,0.45)',
          }}
        >
          Только для администраторов BeeIoT
        </Typography>
      </Paper>
    </Box>
  );
};

export default LoginPage;
